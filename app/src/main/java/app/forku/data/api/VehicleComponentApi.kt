package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleComponentDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleComponentApi {
    @GET("vehicle-component")
    suspend fun getAllComponents(): Response<List<VehicleComponentDto>>

    @GET("vehicle-component/{id}")
    suspend fun getComponentById(@Path("id") id: String): Response<VehicleComponentDto>

    @POST("vehicle-component")
    suspend fun createComponent(@Body component: VehicleComponentDto): Response<VehicleComponentDto>

    @PUT("vehicle-component/{id}")
    suspend fun updateComponent(
        @Path("id") id: String,
        @Body component: VehicleComponentDto
    ): Response<VehicleComponentDto>

    @DELETE("vehicle-component/{id}")
    suspend fun deleteComponent(@Path("id") id: String): Response<Unit>
} 