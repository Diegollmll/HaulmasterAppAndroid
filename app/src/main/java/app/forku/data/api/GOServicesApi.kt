package app.forku.data.api

import app.forku.data.api.dto.goservices.CsrfTokenDto
import app.forku.data.api.dto.goservices.GOServicesSettingsDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Body

/**
 * API interface for GO Services endpoints.
 * All endpoints follow the pattern /dataset/api/goservices/* */ for data operations.
 */
interface GOServicesApi {
    /**
     * Get CSRF token for GO Services API
     * @return CSRF token response
     */
    @GET("dataset/api/goservices/csrf-token")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCsrfToken(): Response<CsrfTokenDto>

    /**
     * Get GO Services settings
     * @return Map of settings key-value pairs
     */
    @GET("dataset/api/goservices/settings")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getSettings(): Response<GOServicesSettingsDto>

    /**
     * Update GO Services settings
     * @param settings The settings to update
     * @return Updated settings
     */
    @POST("dataset/api/goservices/settings")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun updateSettings(@Body settings: GOServicesSettingsDto): Response<GOServicesSettingsDto>
} 