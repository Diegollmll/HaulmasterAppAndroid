package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistChecklistItemCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistChecklistItemCategoryApi {
    @GET("api/checklistchecklistitemcategory/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistChecklistItemCategoryDto>>

    @GET("api/checklistchecklistitemcategory/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistChecklistItemCategoryDto>

    @POST("api/checklistchecklistitemcategory")
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
    ): Response<ChecklistChecklistItemCategoryDto>

    @DELETE("dataset/api/checklistchecklistitemcategory/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklistchecklistitemcategory/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklistchecklistitemcategory/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistChecklistItemCategoryDto>>

    @GET("dataset/api/checklistchecklistitemcategory/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistChecklistItemCategoryDto>

    @POST("dataset/api/checklistchecklistitemcategory")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body category: ChecklistChecklistItemCategoryDto): Response<ChecklistChecklistItemCategoryDto>
} 