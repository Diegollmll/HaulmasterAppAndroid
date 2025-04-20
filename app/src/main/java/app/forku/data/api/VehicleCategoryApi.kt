package app.forku.data.api

import app.forku.data.api.dto.VehicleCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleCategoryApi {
    @GET("vehicle-category")
    suspend fun getVehicleCategories(): Response<List<VehicleCategoryDto>>
    
    @GET("vehicle-category/{id}")
    suspend fun getVehicleCategory(@Path("id") id: String): Response<VehicleCategoryDto>
    
    @POST("vehicle-category")
    suspend fun createVehicleCategory(@Body request: CreateVehicleCategoryRequest): Response<VehicleCategoryDto>
    
    @PUT("vehicle-category/{id}")
    suspend fun updateVehicleCategory(
        @Path("id") id: String,
        @Body request: UpdateVehicleCategoryRequest
    ): Response<VehicleCategoryDto>
    
    @DELETE("vehicle-category/{id}")
    suspend fun deleteVehicleCategory(@Path("id") id: String): Response<Unit>
}

