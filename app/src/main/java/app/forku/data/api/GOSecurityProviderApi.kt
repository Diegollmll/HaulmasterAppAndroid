package app.forku.data.api

import app.forku.data.api.dto.gosecurityprovider.AuthenticationRequest
import app.forku.data.api.dto.gosecurityprovider.AuthenticationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GOSecurityProviderApi {
    /**
     * Authenticate with the GO Security Provider
     * @param request Authentication credentials
     * @return Authentication response
     */
    @POST("api/gosecurityprovider/authenticate")
    suspend fun authenticate(
        @Body request: AuthenticationRequest
    ): Response<AuthenticationResponse>
} 