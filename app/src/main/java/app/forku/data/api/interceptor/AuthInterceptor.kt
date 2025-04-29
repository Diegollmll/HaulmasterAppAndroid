package app.forku.data.api.interceptor

import android.util.Log
import app.forku.data.api.dto.goservices.CsrfTokenDto
import app.forku.data.datastore.AuthDataStore
import app.forku.data.service.GOServicesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import okhttp3.Request
import java.util.Base64
import org.json.JSONObject
import kotlinx.coroutines.runBlocking
import com.google.gson.Gson
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import java.util.concurrent.TimeUnit
import app.forku.core.utils.ApiUtils

private const val TAG = "AuthInterceptor"

/**
 * Error response body format for token-related errors
 */
private data class TokenErrorResponse(
    val code: String,
    val message: String
)

/**
 * Constants for token errors
 */
private object TokenErrors {
    const val EXPIRED_TOKEN = "expiredSecurityToken"
    const val INVALID_TOKEN = "invalidSecurityToken"
    const val NULL_TOKEN = "nullSecurityToken"
    const val MISSING_CSRF = "missingCsrfToken"
}

@Singleton
class AuthInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore,
    private val goServicesManagerProvider: Provider<GOServicesManager>
) : Interceptor {
    private val gson = Gson()
    private var lastKeepAliveTime = 0L
    private val keepAliveIntervalMillis = TimeUnit.MINUTES.toMillis(5) // Keep-alive every 5 minutes

    private fun isTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.d(TAG, "Token format invalid - parts count: ${parts.size}")
                return true
            }
            
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val json = JSONObject(payload)
            val exp = json.getLong("exp")
            val now = System.currentTimeMillis()
            val expMillis = exp * 1000
            
            Log.d(TAG, """
                Token expiration check:
                - Current time: $now
                - Expiration time: $expMillis
                - Time until expiration: ${expMillis - now} ms
                - Is expired: ${(expMillis <= now + 30000)}
            """.trimIndent())
            
            return (expMillis <= now + 30000)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking token expiration", e)
            return true
        }
    }

    private fun buildCookieString(
        applicationToken: String?,
        bearerToken: String?,
        antiforgeryCookie: String? // Use the actual cookie value
    ): String {
        val cookies = mutableListOf<String>()
        
        Log.d(TAG, """
            Building cookie string with:
            - Application token present: ${applicationToken != null}
            - Bearer token present: ${bearerToken != null}
            - Antiforgery cookie present: ${antiforgeryCookie != null}
        """.trimIndent())
        
        // Add ApplicationToken cookie first
        applicationToken?.let {
            cookies.add("ApplicationToken=$it")
        }
        
        // Add BearerToken cookie second
        bearerToken?.let {
            cookies.add("BearerToken=$it")
        }
        
        // Add Antiforgery cookie last (as obtained from server)
        antiforgeryCookie?.let {
            if (!it.contains(";") && !it.contains(",")) {
                cookies.add(it)
            } else {
                Log.w(TAG, "Potentially invalid Antiforgery cookie format: $it")
                val firstPart = it.split(";").firstOrNull()?.split(",")?.firstOrNull()
                firstPart?.let { validPart -> cookies.add(validPart) }
            }
        }
        
        return cookies.joinToString("; ").also {
            Log.d(TAG, "Final cookie string: ${it.take(50)}...")
        }
    }

    // Return Pair<CsrfToken, AntiforgeryCookieValue>
    private suspend fun getCsrfTokenAndCookie(): Pair<String?, String?> {
        Log.d(TAG, "Attempting to get new CSRF token and cookie via GOServicesManager...")
        
        val goServicesManager = goServicesManagerProvider.get()
        return try {
            // GOServicesManager handles fetching and saving both internally now
            val tokenResult = goServicesManager.getCsrfToken(forceRefresh = true)
            
            if (tokenResult.isSuccess) {
                val token = tokenResult.getOrNull()
                val cookie = authDataStore.getAntiforgeryCookie() // Get the potentially updated cookie
            if (token != null && cookie != null) {
                 Log.d(TAG, "Successfully obtained new CSRF token and cookie")
                 Pair(token, cookie)
            } else {
                     Log.e(TAG, "GOServicesManager succeeded but token or cookie is still null. Token: ${token != null}, Cookie: ${cookie != null}")
                     Pair(null, null) // Indicate failure if either is null
                }
            } else {
                 Log.e(TAG, "Failed to get CSRF token/cookie via GOServicesManager: ${tokenResult.exceptionOrNull()?.message}")
                 Pair(null, null) // Explicitly return nulls on failure
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during getCsrfTokenAndCookie", e)
            Pair(null, null) // Return nulls on exception
        }
    }

    /**
     * Attempts to perform a session keep-alive call
     * @return true if successful, false otherwise
     */
    private suspend fun performKeepAlive(chain: Interceptor.Chain, csrfToken: String, antiforgeryCookie: String): Boolean {
        Log.d(TAG, "Performing session keep-alive")
        
        val currentTime = System.currentTimeMillis()
        // Only do keep-alive if it's been at least 5 minutes since the last one
        if (currentTime - lastKeepAliveTime < keepAliveIntervalMillis) {
            Log.d(TAG, "Skipping keep-alive as one was performed recently (${(currentTime - lastKeepAliveTime) / 1000} seconds ago)")
            return true
        }
        
        try {
            val originalRequest = chain.request()
            val keepAliveUrl = ApiUtils.buildApiUrl(
                originalRequest.url.scheme,
                originalRequest.url.host,
                "gosecurityprovider/keepalive"
            )
            
            // Create keep-alive request with CSRF token and cookie
            val keepAliveRequest = Request.Builder()
                .url(keepAliveUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .header("X-CSRF-TOKEN", csrfToken)
                .header("Cookie", antiforgeryCookie)
                .build()
            
            val keepAliveResponse = chain.proceed(keepAliveRequest)
            
            Log.d(TAG, "Keep-alive response: ${keepAliveResponse.code}")
            
            val successful = keepAliveResponse.isSuccessful
            if (successful) {
                lastKeepAliveTime = currentTime
            }
            
            // Always close the response
            keepAliveResponse.close()
            
            return successful
        } catch (e: Exception) {
            Log.e(TAG, "Error performing keep-alive", e)
            return false
        }
    }

    /**
     * Creates a standardized 401 response with error details in the body
     */
    private fun createAuthErrorResponse(
        request: Request, 
        errorCode: String, 
        errorMessage: String
    ): Response {
        Log.w(TAG, "Creating auth error response: $errorCode - $errorMessage")
        
        val errorBody = TokenErrorResponse(errorCode, errorMessage)
        val jsonBody = gson.toJson(errorBody)
        
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401) // Unauthorized
            .message("Authentication Required")
            .body(jsonBody.toResponseBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")
            .build()
    }

    private fun buildErrorResponse(request: Request, errorCode: Int, errorMessage: String): Response {
        val errorJson = JSONObject().apply {
            put("code", errorCode)
            put("message", errorMessage)
        }.toString()

        val errorUrl = ApiUtils.buildApiUrl(
            request.url.scheme,
            request.url.host,
            "error"
        )

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(errorCode)
            .message(errorMessage)
            .body(errorJson.toResponseBody("application/json".toMediaType()))
            .request(Request.Builder().url(errorUrl).build())
            .build()
    }

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        val originalRequest = chain.request()
        
        Log.d(TAG, """
            Starting request interception:
            - URL: ${originalRequest.url}
            - Method: ${originalRequest.method}
        """.trimIndent())
        
        // Skip auth for authentication and CSRF endpoints
        val urlString = originalRequest.url.toString()
        if (urlString.contains("authenticate") || 
            urlString.contains("register") ||
            urlString.contains("csrf-token") ||
            urlString.contains("keepalive")) {
            Log.d(TAG, "Skipping auth for authentication, keepalive, or CSRF endpoint")
            return@runBlocking chain.proceed(originalRequest)
        }

        // Get all tokens from storage
        val applicationToken = authDataStore.getApplicationToken()
        val bearerToken = authDataStore.getAuthenticationToken()
        val csrfToken = authDataStore.getCsrfToken()
        val antiforgeryCookie = authDataStore.getAntiforgeryCookie()
        
        // --- Token Validation ---
        
        // Check for missing application token
        if (applicationToken == null) {
            Log.w(TAG, "Application token is null")
            authDataStore.clearAuth() // Clean up any stale data
            return@runBlocking createAuthErrorResponse(
                originalRequest,
                TokenErrors.NULL_TOKEN,
                "Authentication token not found. Please log in."
            )
        }
        
        // Check for expired token
        if (isTokenExpired(applicationToken)) {
            Log.w(TAG, "Application token has expired")
            authDataStore.clearAuth() // Clean up stale token
            return@runBlocking createAuthErrorResponse(
                originalRequest,
                TokenErrors.EXPIRED_TOKEN,
                "Your session has expired. Please log in again."
            )
        }
        
        // Check for missing CSRF token or cookie
        if (csrfToken == null || antiforgeryCookie == null) {
            Log.w(TAG, "CSRF token or Antiforgery cookie is missing. Attempting to fetch.")
            val (newCsrfToken, newAntiforgeryCookie) = getCsrfTokenAndCookie()

            if (newCsrfToken == null || newAntiforgeryCookie == null) {
                 Log.e(TAG, "Failed to obtain necessary CSRF token/cookie")
                 authDataStore.clearAuth() // Could indicate session issues, clear auth
                 return@runBlocking createAuthErrorResponse(
                     originalRequest,
                     TokenErrors.MISSING_CSRF,
                     "Security token missing. Please log in again."
                 )
            }
            
            // Continue with the newly fetched tokens
            val keepAliveSuccess = performKeepAlive(chain, newCsrfToken, newAntiforgeryCookie)
            if (!keepAliveSuccess) {
                Log.w(TAG, "Keep-alive failed after fetching new CSRF token")
                // We still proceed with the original request, as the keep-alive is just preventive
            }
            
            // --- Build Request with Valid Credentials ---
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "text/plain")
                .header("X-CSRF-TOKEN", newCsrfToken)
            
            // Build cookie header
            val cookieString = buildCookieString(applicationToken, bearerToken, newAntiforgeryCookie)
            if (cookieString.isNotEmpty()) {
                requestBuilder.header("Cookie", cookieString)
            }
            
            val response = chain.proceed(requestBuilder.build())
            
            // Handle 403 responses by attempting to refresh tokens
            if (response.code == 403) {
                Log.w(TAG, "Received 403 response, attempting to refresh tokens")
                response.close()
                
                // Try to get new tokens
                val (refreshedCsrfToken, refreshedCookie) = getCsrfTokenAndCookie()
                if (refreshedCsrfToken != null && refreshedCookie != null) {
                    // Build new request with refreshed tokens
                    val refreshedRequestBuilder = originalRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/plain")
                        .header("X-CSRF-TOKEN", refreshedCsrfToken)
                    
                    val refreshedCookieString = buildCookieString(applicationToken, bearerToken, refreshedCookie)
                    if (refreshedCookieString.isNotEmpty()) {
                        refreshedRequestBuilder.header("Cookie", refreshedCookieString)
                    }
                    
                    return@runBlocking chain.proceed(refreshedRequestBuilder.build())
                }
                
                // If refresh failed, return 401 to trigger re-authentication
                return@runBlocking createAuthErrorResponse(
                    originalRequest,
                    TokenErrors.EXPIRED_TOKEN,
                    "Your session has expired. Please log in again."
                )
            }
            
            return@runBlocking response
        }
        
        // --- Session Maintenance via Keep-Alive ---
        
        // Try keep-alive occasionally to maintain session, but don't fail if it doesn't work
        val shouldPerformKeepAlive = System.currentTimeMillis() - lastKeepAliveTime >= keepAliveIntervalMillis
        if (shouldPerformKeepAlive) {
            try {
                performKeepAlive(chain, csrfToken, antiforgeryCookie)
            } catch (e: Exception) {
                Log.w(TAG, "Keep-alive attempt failed, continuing with original request", e)
                // Continue with the original request regardless
            }
        }
        
        // --- Build Request with Valid Credentials ---
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "text/plain")
            .header("X-CSRF-TOKEN", csrfToken)
        
        // Build cookie header
        val cookieString = buildCookieString(applicationToken, bearerToken, antiforgeryCookie)
        if (cookieString.isNotEmpty()) {
            requestBuilder.header("Cookie", cookieString)
        } else {
            Log.w(TAG, "Cookie string is empty despite having credentials. Check cookie building logic.")
        }
        
        Log.d(TAG, "Proceeding with authenticated request to ${originalRequest.url}")
        val response = chain.proceed(requestBuilder.build())
        
        // Handle 403 responses by attempting to refresh tokens
        if (response.code == 403) {
            Log.w(TAG, "Received 403 response, attempting to refresh tokens")
            response.close()
            
            // Try to get new tokens
            val (refreshedCsrfToken, refreshedCookie) = getCsrfTokenAndCookie()
            if (refreshedCsrfToken != null && refreshedCookie != null) {
                // Build new request with refreshed tokens
                val refreshedRequestBuilder = originalRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/plain")
                    .header("X-CSRF-TOKEN", refreshedCsrfToken)
                
                val refreshedCookieString = buildCookieString(applicationToken, bearerToken, refreshedCookie)
                if (refreshedCookieString.isNotEmpty()) {
                    refreshedRequestBuilder.header("Cookie", refreshedCookieString)
                }
                
                return@runBlocking chain.proceed(refreshedRequestBuilder.build())
            }
            
            // If refresh failed, return 401 to trigger re-authentication
            return@runBlocking createAuthErrorResponse(
                originalRequest,
                TokenErrors.EXPIRED_TOKEN,
                "Your session has expired. Please log in again."
            )
        }
        
        return@runBlocking response
    }
} 