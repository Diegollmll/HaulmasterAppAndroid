package app.forku.data.api

import app.forku.data.api.dto.site.SiteDto
import retrofit2.Response
import retrofit2.http.*

interface SiteApi {
    @GET("business/{businessId}/site")
    suspend fun getSitesByBusiness(@Path("businessId") businessId: String): Response<List<SiteDto>>

    @GET("business/{businessId}/site/{id}")
    suspend fun getSiteById(
        @Path("businessId") businessId: String,
        @Path("id") id: String
    ): Response<SiteDto>

    @POST("business/{businessId}/site")
    suspend fun createSite(
        @Path("businessId") businessId: String,
        @Body site: SiteDto
    ): Response<SiteDto>

    @PUT("business/{businessId}/site/{siteId}")
    suspend fun updateSite(
        @Path("businessId") businessId: String,
        @Path("siteId") siteId: String,
        @Body site: SiteDto
    ): Response<SiteDto>

    @DELETE("business/{businessId}/site/{siteId}")
    suspend fun deleteSite(
        @Path("businessId") businessId: String,
        @Path("siteId") siteId: String
    ): Response<Unit>
} 