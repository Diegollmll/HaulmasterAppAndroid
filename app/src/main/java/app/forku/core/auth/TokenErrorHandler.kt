package app.forku.core.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParseException
import app.forku.data.datastore.AuthDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Error codes that match those defined in the AuthInterceptor
 */
object TokenErrors {
    const val EXPIRED_TOKEN = "expiredSecurityToken"
    const val INVALID_TOKEN = "invalidSecurityToken"
    const val NULL_TOKEN = "nullSecurityToken"
    const val MISSING_CSRF = "missingCsrfToken"
    const val ACCESS_DENIED = "accessDenied"
}

/**
 * Represents a token-related authentication error
 */
data class TokenError(
    val code: String,
    val message: String
)

/**
 * Centralized handler for token authentication errors
 * Monitors for 401 errors, parses them, and signals when re-authentication is needed
 */
@Singleton
class TokenErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authDataStore: AuthDataStore
) {
    private val TAG = "TokenErrorHandler"
    private val gson = Gson()
    
    // StateFlow to observe authentication state
    private val _authenticationState = MutableStateFlow<AuthenticationState>(AuthenticationState.Authenticated)
    val authenticationState: StateFlow<AuthenticationState> = _authenticationState
    
    /**
     * Process an HTTP exception to check if it's a token-related error
     * @return true if this exception was handled as a token error, false otherwise
     */
    suspend fun processError(throwable: Throwable): Boolean {
        if (throwable !is HttpException) return false
        
        val response = throwable.response() ?: return false
        
        // Process both 401 and 403 status codes
        if (response.code() != HttpURLConnection.HTTP_UNAUTHORIZED && 
            response.code() != HttpURLConnection.HTTP_FORBIDDEN) return false
        
        try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrEmpty()) {
                // Generic auth error without error body
                val message = when (response.code()) {
                    HttpURLConnection.HTTP_FORBIDDEN -> "Access denied: Please log in again"
                    else -> "Session expired"
                }
                signalAuthenticationRequired(message)
                return true
            }
            
            val tokenError = parseTokenError(errorBody)
            if (tokenError != null) {
                when (tokenError.code) {
                    TokenErrors.EXPIRED_TOKEN -> {
                        Log.d(TAG, "Token expired: ${tokenError.message}")
                        signalAuthenticationRequired(tokenError.message)
                    }
                    TokenErrors.NULL_TOKEN -> {
                        Log.d(TAG, "Token null: ${tokenError.message}")
                        signalAuthenticationRequired(tokenError.message)
                    }
                    TokenErrors.INVALID_TOKEN -> {
                        Log.d(TAG, "Token invalid: ${tokenError.message}")
                        signalAuthenticationRequired(tokenError.message)
                    }
                    TokenErrors.MISSING_CSRF -> {
                        Log.d(TAG, "CSRF token missing: ${tokenError.message}")
                        signalAuthenticationRequired(tokenError.message)
                    }
                    TokenErrors.ACCESS_DENIED -> {
                        Log.d(TAG, "Access denied: ${tokenError.message}")
                        signalAuthenticationRequired(tokenError.message)
                    }
                    else -> {
                        // Handle generic 403 errors
                        if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                            Log.d(TAG, "Access denied error: ${tokenError.message}")
                            signalAuthenticationRequired("Access denied: ${tokenError.message}")
                        } else {
                            Log.d(TAG, "Other authentication error: ${tokenError.code} - ${tokenError.message}")
                            signalAuthenticationRequired(tokenError.message)
                        }
                    }
                }
                return true
            } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                // Handle 403 errors without proper error body
                Log.d(TAG, "Access denied with unparseable error body: $errorBody")
                signalAuthenticationRequired("Access denied: Please log in again")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing token error response", e)
            if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                signalAuthenticationRequired("Access denied: Please log in again")
                return true
            }
        }
        
        return false
    }
    
    /**
     * Parse a JSON error response into a TokenError object
     */
    private fun parseTokenError(errorBody: String): TokenError? {
        return try {
            gson.fromJson(errorBody, TokenError::class.java)
        } catch (e: JsonParseException) {
            Log.e(TAG, "Failed to parse error body: $errorBody", e)
            null
        }
    }
    
    /**
     * Signal that authentication is required and update the state
     */
    private suspend fun signalAuthenticationRequired(message: String) {
        // Clear credentials
        authDataStore.clearAuth()
        
        // Update state to trigger re-authentication
        _authenticationState.value = AuthenticationState.RequiresAuthentication(message)
        
        Log.d(TAG, "Authentication required: $message")
    }
    
    /**
     * Reset the authentication state to authenticated
     * Should be called after successful login
     */
    fun resetAuthenticationState() {
        _authenticationState.value = AuthenticationState.Authenticated
    }
}

/**
 * Represents the current authentication state of the app
 */
sealed class AuthenticationState {
    object Authenticated : AuthenticationState()
    data class RequiresAuthentication(val message: String) : AuthenticationState()
} 