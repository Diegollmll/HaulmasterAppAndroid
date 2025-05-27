package app.forku.data.api

import app.forku.data.dto.HazardIncidentDto
import retrofit2.http.*

interface HazardIncidentApi {
    @GET("api/hazardincident/byid/{id}")
    suspend fun getById(@Path("id") id: String): HazardIncidentDto

    @GET("api/hazardincident/list")
    suspend fun getList(): List<HazardIncidentDto>

    @GET("api/hazardincident/count")
    suspend fun getCount(): Int

    @FormUrlEncoded
    @POST("api/hazardincident")
    suspend fun save(
        @Field("entity") entity: String,
        @Field("include") include: String? = null,
        @Field("dateformat") dateformat: String? = "ISO8601"
    ): HazardIncidentDto

    @DELETE("api/hazardincident/{id}")
    suspend fun deleteById(@Path("id") id: String)
} 