package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleComponentDto
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Headers

interface VehicleComponentApi {
    @GET("api/vehiclecomponent/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllComponents(): Response<List<VehicleComponentDto>>

    @GET("api/vehiclecomponent/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getComponentById(@Path("id") id: String): Response<VehicleComponentDto>

    @POST("api/vehiclecomponent")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveComponent(@Body component: VehicleComponentDto): Response<VehicleComponentDto>

    @DELETE("dataset/api/vehiclecomponent/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteComponent(@Path("id") id: String): Response<Unit>
} 