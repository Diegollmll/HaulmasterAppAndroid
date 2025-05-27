package app.forku.data.api

import app.forku.data.dto.NearMissIncidentDto
import retrofit2.http.*

interface NearMissIncidentApi {
    @GET("api/nearmissincident/byid/{id}")
    suspend fun getById(@Path("id") id: String): NearMissIncidentDto

    @GET("api/nearmissincident/list")
    suspend fun getList(): List<NearMissIncidentDto>

    @GET("api/nearmissincident/count")
    suspend fun getCount(): Int

    @FormUrlEncoded
    @POST("api/nearmissincident")
    suspend fun save(
        @Field("entity") entity: String,
        @Field("include") include: String? = null,
        @Field("dateformat") dateformat: String? = "ISO8601"
    ): NearMissIncidentDto

    @DELETE("api/nearmissincident/{id}")
    suspend fun deleteById(@Path("id") id: String)
} 