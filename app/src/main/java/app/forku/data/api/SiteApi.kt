package app.forku.data.api

import app.forku.data.api.dto.site.SiteDto
import retrofit2.Response
import retrofit2.http.*

interface SiteApi {
    @GET("api/site/list")
    suspend fun getAllSites(): Response<List<SiteDto>>

    @GET("api/site/byid/{id}")
    suspend fun getSiteById(@Path("id") id: String): Response<SiteDto>

    @POST("api/site")
    suspend fun saveSite(@Body site: SiteDto): Response<SiteDto>

    @DELETE("dataset/api/site/{id}")
    suspend fun deleteSite(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/site/count")
    suspend fun getSiteCount(): Response<Int>
} 