package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistItemCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistItemCategoryApi {
    @GET("api/checklistitemcategory/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistItemCategoryDto>>

    @GET("api/checklistitemcategory/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistItemCategoryDto>

    @POST("api/checklistitemcategory")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun save(@Body category: ChecklistItemCategoryDto): Response<ChecklistItemCategoryDto>

    @DELETE("dataset/api/checklistitemcategory/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklistitemcategory/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklistitemcategory/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistItemCategoryDto>>

    @GET("dataset/api/checklistitemcategory/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistItemCategoryDto>

    @POST("dataset/api/checklistitemcategory")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body category: ChecklistItemCategoryDto): Response<ChecklistItemCategoryDto>
} 