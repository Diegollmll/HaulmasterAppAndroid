package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistItemSubcategoryDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistItemSubcategoryApi {
    @GET("api/checklistitemsubcategory/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistItemSubcategoryDto>>

    @GET("api/checklistitemsubcategory/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistItemSubcategoryDto>

    @POST("api/checklistitemsubcategory")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun save(@Body subcategory: ChecklistItemSubcategoryDto): Response<ChecklistItemSubcategoryDto>

    @DELETE("dataset/api/checklistitemsubcategory/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklistitemsubcategory/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklistitemsubcategory/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistItemSubcategoryDto>>

    @GET("dataset/api/checklistitemsubcategory/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistItemSubcategoryDto>

    @POST("dataset/api/checklistitemsubcategory")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body subcategory: ChecklistItemSubcategoryDto): Response<ChecklistItemSubcategoryDto>
} 