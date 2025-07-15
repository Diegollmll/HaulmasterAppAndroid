package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistVehicleTypeDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistVehicleTypeApi {
    @GET("api/checklistvehicletype/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistVehicleTypeDto>>

    @GET("api/checklistvehicletype/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistVehicleTypeDto>

    @POST("api/checklistvehicletype")
    @FormUrlEncoded
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun save(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String,
        @Query("businessId") businessId: String? = null
    ): Response<ChecklistVehicleTypeDto>

    @DELETE("dataset/api/checklistvehicletype/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklistvehicletype/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklistvehicletype/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistVehicleTypeDto>>

    @GET("dataset/api/checklistvehicletype/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistVehicleTypeDto>

    @POST("dataset/api/checklistvehicletype")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body vehicleType: ChecklistVehicleTypeDto): Response<ChecklistVehicleTypeDto>
} 