package app.forku.data.api.interceptor

import android.util.Log
import app.forku.core.auth.HeaderManager
import app.forku.data.datastore.AuthDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Request
import kotlinx.coroutines.runBlocking
import com.google.gson.Gson
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import app.forku.core.utils.ApiUtils
import app.forku.core.auth.TokenErrorHandler
import app.forku.data.api.dto.error.AuthErrorDto
import retrofit2.HttpException
import app.forku.data.api.dto.toApiResponse
import app.forku.data.api.dto.ApiResponse

private const val TAG = "appflow AuthInterceptor"

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
    private val headerManager: HeaderManager,
    private val tokenErrorHandler: TokenErrorHandler,
    private val sessionKeepAliveManager: app.forku.core.auth.SessionKeepAliveManager? = null // ✅ NEW: Inject SessionKeepAliveManager
) : Interceptor {
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val originalRequest = chain.request()
            
            Log.d(TAG, """
                Starting request interception:
                - URL: ${originalRequest.url}
                - Method: ${originalRequest.method}
            """.trimIndent())
            
            // Skip auth for authentication and CSRF endpoints
            // NOTE: renewtoken is NOT excluded - let's see what happens
            val urlString = originalRequest.url.toString()
            if (urlString.contains("authenticate") || 
                urlString.contains("register") ||
                urlString.contains("csrf-token") ||
                urlString.contains("keepalive")) {
                Log.d(TAG, "Skipping auth for authentication, keepalive, or CSRF endpoint")
                return@runBlocking chain.proceed(originalRequest)
            }
            
            // 🔍 SPECIAL LOGGING FOR RENEWTOKEN
            if (urlString.contains("renewtoken")) {
                Log.d(TAG, "🔍 RENEWTOKEN ENDPOINT DETECTED - Will process with AuthInterceptor")
            }

            // Get headers from HeaderManager
            val headersResult = headerManager.getHeaders()
            if (headersResult.isFailure) {
                val exception = headersResult.exceptionOrNull()
                Log.e(TAG, "Failed to get headers: ${exception?.message}")
                
                // ✅ NEW: Check if this is a token expiration issue
                if (exception?.message?.contains("No application token found") == true) {
                    Log.w(TAG, "🚨 No application token found - this may indicate session expiration")
                    // Try to trigger token renewal before giving up
                    sessionKeepAliveManager?.let { manager ->
                        Log.d(TAG, "🔄 Attempting emergency token renewal")
                        val renewalSuccess = manager.performTokenRenewal()
                        if (renewalSuccess) {
                            Log.d(TAG, "✅ Emergency token renewal successful - retrying request")
                            // Retry the request with new tokens
                            val retryHeadersResult = headerManager.getHeaders(forceRefresh = true)
                            if (retryHeadersResult.isSuccess) {
                                return@runBlocking proceedWithHeaders(chain, retryHeadersResult.getOrNull()!!, urlString)
                            } else {
                                Log.e(TAG, "❌ Failed to get headers after renewal")
                            }
                        } else {
                            Log.e(TAG, "❌ Emergency token renewal failed - session truly expired")
                        }
                    }
                }
                // Si no se retorna antes, retornar error de autenticación
                return@runBlocking createAuthErrorResponse(
                    originalRequest,
                    "authError",
                    exception?.message ?: "Authentication error"
                )
            }

            val headers = headersResult.getOrNull()!!
            return@runBlocking proceedWithHeaders(chain, headers, urlString)
        } ?: error("Unreachable code in AuthInterceptor: runBlocking did not return a Response")
    }
    
    // ✅ NEW: Extract request processing logic
    private suspend fun proceedWithHeaders(chain: Interceptor.Chain, headers: app.forku.core.auth.HeaderManager.Headers, urlString: String): Response {
        // Build request with headers
        val requestBuilder = chain.request().newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "text/plain")
            .header("X-CSRF-TOKEN", headers.csrfToken)

        // Build cookie string
        val cookieString = buildCookieString(
            applicationToken = headers.applicationToken,
            antiforgeryCookie = headers.cookie
        )
        
        // 🔍 DETAILED LOGGING FOR RENEWTOKEN
        if (urlString.contains("renewtoken")) {
            Log.d(TAG, "🔍 RENEWTOKEN Headers being built:")
            Log.d(TAG, "  - ApplicationToken: ${headers.applicationToken?.take(20)}...")
            Log.d(TAG, "  - Antiforgery Cookie: ${headers.cookie.take(50)}...")
            Log.d(TAG, "  - Built Cookie String: ${cookieString.take(100)}...")
            Log.d(TAG, "  - Cookie Length: ${cookieString.length}")
        }
        
        if (cookieString.isNotEmpty()) {
            requestBuilder.header("Cookie", cookieString)
        }

        // Make the request
        val response = chain.proceed(requestBuilder.build())

        // 🔍 SPECIAL HANDLING FOR RENEWTOKEN RESPONSES
        if (urlString.contains("renewtoken")) {
            Log.d(TAG, "🔍 RENEWTOKEN Response: code=${response.code}, successful=${response.isSuccessful}")
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "🚨 RENEWTOKEN FAILED:")
                Log.e(TAG, "  - Status Code: ${response.code}")
                Log.e(TAG, "  - Error Body: $errorBody")
                Log.e(TAG, "  - Headers Sent: CSRF=${headers.csrfToken.take(20)}..., Cookie Length=${headers.cookie.length}")
                
                // For renewtoken failures, let's still return the response as-is
                // The calling code should handle the failure appropriately
                return response
            } else {
                Log.d(TAG, "✅ RENEWTOKEN SUCCESS: ${response.code}")
                // ✅ NEW: Update session keep-alive after successful renewal
                sessionKeepAliveManager?.let { manager ->
                    Log.d(TAG, "🔄 Updating session keep-alive after successful token renewal")
                    manager.performKeepAlive()
                }
            }
        }

        // Handle auth errors
        if (response.code == 401 || response.code == 403) {
            val errorBody = response.body?.string()
            if (errorBody != null) {
                try {
                    val authError = gson.fromJson(errorBody, AuthErrorDto::class.java)
                    if (authError.isTokenExpired() || authError.isAuthError()) {
                        Log.w(TAG, "🚨 Token expired or auth error detected: ${authError.detail}")
                        
                        // ✅ NEW: Update session keep-alive state before clearing auth
                        sessionKeepAliveManager?.let { manager ->
                            Log.d(TAG, "🛑 Stopping session keep-alive due to token expiration")
                            manager.stopKeepAlive()
                        }
                        
                        // Clear auth and notify TokenErrorHandler
                        headerManager.clearAuth()
                        tokenErrorHandler.processError(
                            HttpException(retrofit2.Response.error<Any>(
                                response.code,
                                errorBody.toResponseBody("application/json".toMediaType())
                            ))
                        )
                        
                        // Return error response
                        return createAuthErrorResponse(
                            chain.request(),
                            "tokenExpired",
                            authError.detail ?: "Session expired. Please log in again"
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse error as AuthErrorDto", e)
                }
            }
            
            // For other 401/403 errors, try refresh token
            if (response.code == 403) {
                Log.d(TAG, "🔄 403 error detected - attempting token refresh")
                val refreshedHeadersResult = headerManager.getHeaders(forceRefresh = true)
                if (refreshedHeadersResult.isSuccess) {
                    val refreshedHeaders = refreshedHeadersResult.getOrNull()!!
                    val refreshedRequest = chain.request().newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/plain")
                        .header("X-CSRF-TOKEN", refreshedHeaders.csrfToken)
                        .header("Cookie", buildCookieString(
                            applicationToken = refreshedHeaders.applicationToken,
                            antiforgeryCookie = refreshedHeaders.cookie
                        ))
                        .build()
                    return chain.proceed(refreshedRequest)
                } else {
                    Log.w(TAG, "❌ Token refresh failed for 403 error")
                }
            }
            
            // If we get here, authentication failed
            Log.e(TAG, "💀 Authentication failed - clearing auth and stopping session")
            headerManager.clearAuth()
            
            // ✅ NEW: Stop session keep-alive on auth failure
            sessionKeepAliveManager?.let { manager ->
                Log.d(TAG, "🛑 Stopping session keep-alive due to authentication failure")
                manager.stopKeepAlive()
            }
            
            tokenErrorHandler.processError(
                HttpException(retrofit2.Response.error<Any>(
                    response.code,
                    (errorBody ?: "Authentication failed").toResponseBody("application/json".toMediaType())
                ))
            )
            return createAuthErrorResponse(
                chain.request(),
                "unauthorized",
                "Please log in again"
            )
        }

        return response
    }

    private fun buildCookieString(
        applicationToken: String?,
        antiforgeryCookie: String?
    ): String {
        val cookies = mutableListOf<String>()
        
        // Add ApplicationToken
        applicationToken?.let {
            cookies.add("ApplicationToken=$it")
            // Also add BearerToken with same value but different expiration
            cookies.add("BearerToken=$it")
            Log.d(TAG, "🔍 Added ApplicationToken and BearerToken to cookie")
        } ?: Log.d(TAG, "🔍 No ApplicationToken available")
        
        // Add Antiforgery cookie with improved cleaning
        antiforgeryCookie?.let { rawCookie ->
            val cleanedCookie = cleanAntiforgeryCookie(rawCookie)
            if (cleanedCookie.isNotEmpty()) {
                cookies.add(cleanedCookie)
                Log.d(TAG, "🔍 Added cleaned Antiforgery cookie: ${cleanedCookie.take(50)}...")
            } else {
                Log.w(TAG, "⚠️ Could not clean Antiforgery cookie: $rawCookie")
            }
        } ?: Log.d(TAG, "🔍 No Antiforgery cookie available")
        
        val result = cookies.joinToString("; ")
        Log.d(TAG, "🔍 Final cookie string length: ${result.length}")
        return result
    }

    /**
     * Cleans Antiforgery cookie by removing server metadata
     */
    private fun cleanAntiforgeryCookie(rawCookie: String): String {
        return try {
            when {
                // Standard format: name=value
                !rawCookie.contains(";") && !rawCookie.contains(",") -> rawCookie
                
                // Format with metadata: name=value; path=/...; samesite=strict; httponly
                rawCookie.contains(";") -> {
                    val cookieParts = rawCookie.split(";")
                    val mainPart = cookieParts.first().trim()
                    
                    // Validate it looks like a proper cookie (name=value)
                    if (mainPart.contains("=") && mainPart.startsWith(".AspNetCore.Antiforgery")) {
                        Log.d(TAG, "🧹 Cleaned cookie from '${rawCookie.take(100)}...' to '$mainPart'")
                        mainPart
                    } else {
                        Log.w(TAG, "⚠️ Invalid cookie main part: $mainPart")
                        ""
                    }
                }
                
                // Format with comma separators
                rawCookie.contains(",") -> {
                    val cookieParts = rawCookie.split(",")
                    val mainPart = cookieParts.first().trim()
                    
                    if (mainPart.contains("=") && mainPart.startsWith(".AspNetCore.Antiforgery")) {
                        Log.d(TAG, "🧹 Cleaned comma-separated cookie to '$mainPart'")
                        mainPart
                    } else {
                        Log.w(TAG, "⚠️ Invalid comma-separated cookie: $mainPart")
                        ""
                    }
                }
                
                else -> {
                    Log.w(TAG, "⚠️ Unknown cookie format: $rawCookie")
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cleaning cookie: $rawCookie", e)
            ""
        }
    }

    private fun createAuthErrorResponse(
        request: Request,
        code: String,
        message: String
    ): Response {
        val authError = AuthErrorDto(
            title = code,
            detail = message,
            status = if (code == "unauthorized") 401 else 403
        )
        val jsonBody = gson.toJson(authError)
        
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(if (code == "unauthorized") 401 else 403)
            .message("Authentication Required")
            .body(jsonBody.toResponseBody("application/json".toMediaType()))
            .build()
    }
} 