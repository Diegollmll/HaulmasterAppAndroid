package app.forku.data.api

import app.forku.data.dto.CollisionIncidentDto
import retrofit2.http.*

interface CollisionIncidentApi {
    @GET("api/collisionincident/byid/{id}")
    suspend fun getById(@Path("id") id: String): CollisionIncidentDto

    @GET("api/collisionincident/list")
    suspend fun getList(): List<CollisionIncidentDto>

    @GET("api/collisionincident/count")
    suspend fun getCount(): Int

    @FormUrlEncoded
    @POST("api/collisionincident")
    suspend fun save(
        @Field("entity") entity: String,
        @Field("include") include: String? = null,
        @Field("dateformat") dateformat: String? = "ISO8601",
        @Query("businessId") businessId: String? = null
    ): CollisionIncidentDto

    @DELETE("api/collisionincident/{id}")
    suspend fun deleteById(@Path("id") id: String)
} 