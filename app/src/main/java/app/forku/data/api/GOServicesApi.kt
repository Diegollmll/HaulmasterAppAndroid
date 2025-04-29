package app.forku.data.api

import app.forku.data.api.dto.goservices.CsrfTokenDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface GOServicesApi {
    /**
     * Get CSRF token for GO Services API
     * @return CSRF token response
     */
    @GET("dataset/api/goservices/csrf-token")
    suspend fun getCsrfToken(): Response<CsrfTokenDto>

    @GET("dataset/api/goservices/settings")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getSettings(): Response<Map<String, Any>>
} 