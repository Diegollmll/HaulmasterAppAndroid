package app.forku.core.auth

import android.util.Log
import javax.inject.Inject

/**
 * Base class for repositories that need to handle authentication errors
 * Provides a standardized way to execute authenticated API calls and handle token errors
 */
abstract class AuthenticatedRepository {
    
    @Inject
    lateinit var tokenErrorHandler: TokenErrorHandler
    
    private val TAG = "AuthenticatedRepository"
    
    /**
     * Execute an authenticated API call and handle token errors
     * @param apiCall the suspend function representing the API call
     * @return Result containing the API response or an error
     */
    protected suspend fun <T> executeAuthenticatedCall(apiCall: suspend () -> T): Result<T> {
        return try {
            // Execute the API call
            val result = apiCall()
            Result.success(result)
        } catch (e: Exception) {
            // Let the token error handler process the exception first
            val handled = tokenErrorHandler.processError(e)
            
            if (handled) {
                // If it was a token error, return a specific auth error
                Log.d(TAG, "API call failed due to authentication error")
                Result.failure(AuthenticationException("Authentication required"))
            } else {
                // If it wasn't a token error, just pass through the original exception
                Log.e(TAG, "API call failed", e)
                Result.failure(e)
            }
        }
    }
}

/**
 * Exception thrown when an API call fails due to authentication issues
 */
class AuthenticationException(message: String) : Exception(message) 