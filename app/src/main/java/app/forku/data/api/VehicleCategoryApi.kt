package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleCategoryApi {
    @GET("vehicle-category")
    suspend fun getAllCategories(): Response<List<VehicleCategoryDto>>

    @GET("vehicle-category/{id}")
    suspend fun getCategoryById(@Path("id") id: String): Response<VehicleCategoryDto>

    @POST("vehicle-category")
    suspend fun createCategory(@Body category: VehicleCategoryDto): Response<VehicleCategoryDto>

    @PUT("vehicle-category/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body category: VehicleCategoryDto
    ): Response<VehicleCategoryDto>

    @DELETE("vehicle-category/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>
} 