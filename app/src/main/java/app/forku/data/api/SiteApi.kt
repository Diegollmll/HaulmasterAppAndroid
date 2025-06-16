package app.forku.data.api

import app.forku.data.api.dto.site.SiteDto
import retrofit2.Response
import retrofit2.http.*

interface SiteApi {
    @GET("api/site/list")
    suspend fun getAllSites(
        @Query("include") include: String? = null,
        @Query("filter") filter: String? = null
    ): Response<List<SiteDto>>

    @GET("api/site/byid/{id}")
    suspend fun getSiteById(
        @Path("id") id: String,
        @Query("include") include: String? = null
    ): Response<SiteDto>

    @POST("api/site")
    suspend fun saveSite(
        @Body site: SiteDto,
        @Query("include") include: String? = null
    ): Response<SiteDto>

    @DELETE("dataset/api/site/{id}")
    suspend fun deleteSite(
        @Path("id") id: String
    ): Response<Unit>

    @GET("dataset/api/site/count")
    suspend fun getSiteCount(
        @Query("filter") filter: String? = null
    ): Response<Int>
} 