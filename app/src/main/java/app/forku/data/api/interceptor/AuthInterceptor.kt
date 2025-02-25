package app.forku.data.api.interceptor

import app.forku.data.api.Sub7Api
import app.forku.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import kotlinx.coroutines.runBlocking
import app.forku.data.api.dto.user.RefreshTokenRequestDto

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val api: Sub7Api
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenManager.getToken()

        // Add token to request if exists
        val authenticatedRequest = if (token != null) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else request

        // Try the request
        var response = chain.proceed(authenticatedRequest)

        // If unauthorized, try to refresh token
        if (response.code == 401) {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                try {
                    runBlocking {
                        val newTokenResponse = api.refreshToken(RefreshTokenRequestDto(refreshToken))
                        val newTokens = newTokenResponse.body()
                        if (newTokens != null) {
                            tokenManager.saveToken(
                                token = newTokens.token,
                                refreshToken = newTokens.refreshToken
                            )
                        }
                    }

                    // Retry with new token
                    response.close()
                    return chain.proceed(
                        request.newBuilder()
                            .header("Authorization", "Bearer ${tokenManager.getToken()}")
                            .build()
                    )
                } catch (e: Exception) {
                    // Refresh failed, clear tokens
                    tokenManager.clearToken()
                }
            }
        }

        return response
    }
}