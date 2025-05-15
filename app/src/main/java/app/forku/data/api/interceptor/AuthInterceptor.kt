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
    private val tokenErrorHandler: TokenErrorHandler
) : Interceptor {
    private val gson = Gson()

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

        // Get headers from HeaderManager
        val headersResult = headerManager.getHeaders()
        if (headersResult.isFailure) {
            val exception = headersResult.exceptionOrNull()
            Log.e(TAG, "Failed to get headers: ${exception?.message}")
            return@runBlocking createAuthErrorResponse(
                originalRequest,
                "authError",
                exception?.message ?: "Authentication error"
            )
        }

        val headers = headersResult.getOrNull()!!
        
        // Build request with headers
        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "text/plain")
            .header("X-CSRF-TOKEN", headers.csrfToken)

        // Build cookie string
        val cookieString = buildCookieString(
            applicationToken = headers.applicationToken,
            antiforgeryCookie = headers.cookie
        )
        
        if (cookieString.isNotEmpty()) {
            requestBuilder.header("Cookie", cookieString)
        }

        // Make the request
        val response = chain.proceed(requestBuilder.build())

        // Handle auth errors
        if (response.code == 401 || response.code == 403) {
            val errorBody = response.body?.string()
            if (errorBody != null) {
                try {
                    val authError = gson.fromJson(errorBody, AuthErrorDto::class.java)
                    if (authError.isTokenExpired() || authError.isAuthError()) {
                        // Clear auth and notify TokenErrorHandler
                        headerManager.clearAuth()
                        tokenErrorHandler.processError(
                            HttpException(retrofit2.Response.error<Any>(
                                response.code,
                                errorBody.toResponseBody("application/json".toMediaType())
                            ))
                        )
                        
                        // Return error response
                        return@runBlocking createAuthErrorResponse(
                            originalRequest,
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
                val refreshedHeadersResult = headerManager.getHeaders(forceRefresh = true)
                if (refreshedHeadersResult.isSuccess) {
                    val refreshedHeaders = refreshedHeadersResult.getOrNull()!!
                    val refreshedRequest = originalRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/plain")
                        .header("X-CSRF-TOKEN", refreshedHeaders.csrfToken)
                        .header("Cookie", buildCookieString(
                            applicationToken = refreshedHeaders.applicationToken,
                            antiforgeryCookie = refreshedHeaders.cookie
                        ))
                        .build()
                    return@runBlocking chain.proceed(refreshedRequest)
                }
            }
            
            // If we get here, authentication failed
            headerManager.clearAuth()
            tokenErrorHandler.processError(
                HttpException(retrofit2.Response.error<Any>(
                    response.code,
                    (errorBody ?: "Authentication failed").toResponseBody("application/json".toMediaType())
                ))
            )
            return@runBlocking createAuthErrorResponse(
                originalRequest,
                "unauthorized",
                "Please log in again"
            )
        }

        response
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
        }
        
        // Add Antiforgery cookie
        antiforgeryCookie?.let {
            if (!it.contains(";") && !it.contains(",")) {
                cookies.add(it)
            } else {
                Log.w(TAG, "Invalid Antiforgery cookie format: $it")
                val firstPart = it.split(";").firstOrNull()?.split(",")?.firstOrNull()
                firstPart?.let { validPart -> cookies.add(validPart) }
            }
        }
        
        return cookies.joinToString("; ")
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