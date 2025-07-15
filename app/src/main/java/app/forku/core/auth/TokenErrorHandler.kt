package app.forku.core.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParseException
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.error.AuthErrorDto
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
 * Monitors for 401/403 errors, parses them, and signals when re-authentication is needed
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
            
            // Try to parse as AuthErrorDto first
            try {
                val authError = gson.fromJson(errorBody, AuthErrorDto::class.java)
                if (authError != null) {
                    when {
                        authError.isTokenExpired() -> {
                            Log.d(TAG, "Token expired: ${authError.detail}")
                            signalAuthenticationRequired(authError.detail ?: "Session expired")
                            return true
                        }
                        authError.isAuthError() -> {
                            Log.d(TAG, "Auth error: ${authError.detail}")
                            signalAuthenticationRequired(authError.detail ?: "Authentication required")
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to old parsing if AuthErrorDto fails
                val tokenError = parseTokenError(errorBody)
                if (tokenError != null) {
                    handleTokenError(tokenError)
                    return true
                }
            }
            
            // If we get here and it's a 403, treat as auth error
            if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                Log.d(TAG, "Generic 403 error")
                signalAuthenticationRequired("Access denied: Please log in again")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing auth error", e)
            if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                signalAuthenticationRequired("Access denied: Please log in again")
                return true
            }
        }
        
        return false
    }
    
    private suspend fun handleTokenError(tokenError: TokenError) {
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
                Log.d(TAG, "Other auth error: ${tokenError.code} - ${tokenError.message}")
                signalAuthenticationRequired(tokenError.message)
            }
        }
    }
    
    /**
     * Parse a JSON error response into a TokenError object
     */
    private fun parseTokenError(errorBody: String): TokenError? {
        return try {
            val json = gson.fromJson(errorBody, Map::class.java)
            val code = json["code"] as? String ?: json["title"] as? String
            val message = json["message"] as? String ?: json["detail"] as? String ?: ""
            if (code != null) TokenError(code, message) else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse error body: $errorBody", e)
            null
        }
    }
    
    /**
     * Signal that authentication is required and update the state
     */
    suspend fun signalAuthenticationRequired(message: String) {
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
        // Validar token y usuario antes de setear Authenticated
        val token = authDataStore.getApplicationToken()
        val user = kotlin.runCatching { kotlinx.coroutines.runBlocking { authDataStore.getCurrentUser() } }.getOrNull()
        val isTokenValid = authDataStore.isTokenValid()
        if (!token.isNullOrBlank() && isTokenValid && user != null && !user.id.isNullOrBlank()) {
        _authenticationState.value = AuthenticationState.Authenticated
        } else {
            val reason = when {
                token.isNullOrBlank() -> "No valid token found."
                !isTokenValid -> "Token is expired or invalid."
                user == null || user.id.isNullOrBlank() -> "No valid user found."
                else -> "Unknown authentication error."
            }
            _authenticationState.value = AuthenticationState.RequiresAuthentication(reason)
            Log.w(TAG, "[SECURITY] Tried to set Authenticated state but failed: $reason")
        }
    }
}

/**
 * Represents the current authentication state of the app
 */
sealed class AuthenticationState {
    object Authenticated : AuthenticationState()
    data class RequiresAuthentication(val message: String) : AuthenticationState()
} 