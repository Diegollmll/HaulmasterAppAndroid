package app.forku.data.api

import app.forku.data.api.dto.vehicle.VehicleCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface VehicleCategoryApi {
    @GET("api/vehiclecategory/list")
    suspend fun getVehicleCategories(): Response<List<VehicleCategoryDto>>
    
    @GET("api/vehiclecategory/byid/{id}")
    suspend fun getVehicleCategory(@Path("id") id: String): Response<VehicleCategoryDto>
    
    @FormUrlEncoded
    @POST("api/vehiclecategory")
    suspend fun saveVehicleCategory(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String
    ): Response<VehicleCategoryDto>
    
    @DELETE("dataset/api/vehiclecategory/{id}")
    suspend fun deleteVehicleCategory(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>
}

