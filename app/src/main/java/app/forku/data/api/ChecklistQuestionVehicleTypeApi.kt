package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistQuestionVehicleTypeDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistQuestionVehicleTypeApi {
    @GET("api/checklistquestionvehicletype/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistQuestionVehicleTypeDto>>

    @GET("api/checklistquestionvehicletype/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistQuestionVehicleTypeDto>

    @POST("api/checklistquestionvehicletype")
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
    ): Response<ChecklistQuestionVehicleTypeDto>

    @DELETE("dataset/api/checklistquestionvehicletype/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklistquestionvehicletype/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklistquestionvehicletype/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistQuestionVehicleTypeDto>>

    @GET("dataset/api/checklistquestionvehicletype/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistQuestionVehicleTypeDto>

    @POST("dataset/api/checklistquestionvehicletype")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body questionVehicleType: ChecklistQuestionVehicleTypeDto): Response<ChecklistQuestionVehicleTypeDto>
} 