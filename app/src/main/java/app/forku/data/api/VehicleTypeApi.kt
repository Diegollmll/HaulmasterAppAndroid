package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleTypeDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleTypeApi {
    @GET("api/vehicletype/list")
    suspend fun getAllVehicleTypes(): Response<List<VehicleTypeDto>>

    @GET("api/vehicletype/byid/{id}")
    suspend fun getVehicleTypeById(@Path("id") id: String): Response<VehicleTypeDto>

    @POST("api/vehicletype")
    suspend fun saveVehicleType(@Body vehicleType: VehicleTypeDto): Response<VehicleTypeDto>

    @DELETE("dataset/api/vehicletype/{id}")
    suspend fun deleteVehicleType(@Path("id") id: String): Response<Unit>

    @GET("api/vehicletype/list")
    suspend fun getVehicleTypesByCategory(@Query("categoryId") categoryId: String): Response<List<VehicleTypeDto>>
} 