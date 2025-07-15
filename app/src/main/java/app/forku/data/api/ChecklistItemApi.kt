package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistItemDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistItemApi {
    @GET("api/checklistitem/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistItemDto>>

    @GET("api/checklistitem/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistItemDto>

    @FormUrlEncoded
    @POST("api/checklistitem")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun save(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String,
        @Query("businessId") businessId: String? = null
    ): Response<ChecklistItemDto>

    @DELETE("dataset/api/checklistitem/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklistitem/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklistitem/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistItemDto>>

    @GET("dataset/api/checklistitem/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistItemDto>

    @POST("dataset/api/checklistitem")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body item: ChecklistItemDto): Response<ChecklistItemDto>
} 