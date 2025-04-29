package app.forku.data.api

import app.forku.data.api.dto.gosecurityprovider.AuthenticationRequest
import app.forku.data.api.dto.gosecurityprovider.AuthenticationResponse
import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody

interface GOSecurityProviderApi {
    /**
     * Authenticate with the GO Security Provider
     * @param username Username for authentication
     * @param password Password for authentication
     * @param useCookies Whether to use cookies (defaults to true)
     * @return Authentication response
     */
    @Multipart
    @POST("api/gosecurityprovider/authenticate")
    suspend fun authenticate(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part("useCookies") useCookies: RequestBody
    ): Response<AuthenticationResponse>
} 