package app.forku.data.api

import app.forku.data.dto.VehicleFailIncidentDto
import retrofit2.http.*

interface VehicleFailIncidentApi {
    @GET("api/vehiclefailincident/byid/{id}")
    suspend fun getById(@Path("id") id: String): VehicleFailIncidentDto

    @GET("api/vehiclefailincident/list")
    suspend fun getList(): List<VehicleFailIncidentDto>

    @GET("api/vehiclefailincident/count")
    suspend fun getCount(): Int

    @FormUrlEncoded
    @POST("api/vehiclefailincident")
    suspend fun save(
        @Field("entity") entity: String,
        @Field("include") include: String? = null,
        @Field("dateformat") dateformat: String? = "ISO8601"
    ): VehicleFailIncidentDto

    @DELETE("api/vehiclefailincident/{id}")
    suspend fun deleteById(@Path("id") id: String)
} 