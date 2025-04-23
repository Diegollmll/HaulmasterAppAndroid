package app.forku.data.api

import app.forku.data.api.dto.goservices.CsrfTokenDto
import retrofit2.Response
import retrofit2.http.GET

interface GOServicesApi {
    /**
     * Get CSRF token for GO Services API
     * @return CSRF token response
     */
    @GET("dataset/api/goservices/csrf-token")
    suspend fun getCsrfToken(): Response<CsrfTokenDto>
} 