package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleTypeDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleTypeApi {
    @GET("vehicle-type")
    suspend fun getAllVehicleTypes(): Response<List<VehicleTypeDto>>

    @GET("vehicle-type/{id}")
    suspend fun getVehicleTypeById(@Path("id") id: String): Response<VehicleTypeDto>

    @GET("vehicle-type")
    suspend fun getVehicleTypesByCategory(@Query("categoryId") categoryId: String): Response<List<VehicleTypeDto>>

    @POST("vehicle-type")
    suspend fun createVehicleType(@Body vehicleType: VehicleTypeDto): Response<VehicleTypeDto>

    @PUT("vehicle-type/{id}")
    suspend fun updateVehicleType(
        @Path("id") id: String,
        @Body vehicleType: VehicleTypeDto
    ): Response<VehicleTypeDto>

    @DELETE("vehicle-type/{id}")
    suspend fun deleteVehicleType(@Path("id") id: String): Response<Unit>
} 