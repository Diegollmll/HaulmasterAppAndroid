package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistDto
import app.forku.data.api.dto.checklist.PreShiftCheckDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistApi {
    @GET("api/checklist/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistDto>>

    @GET("api/checklist/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistDto>

    @POST("api/checklist")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun save(@Body checklist: ChecklistDto): Response<ChecklistDto>

    @DELETE("dataset/api/checklist/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/checklist/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCount(): Response<Int>

    @GET("dataset/api/checklist/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getListDataset(): Response<List<ChecklistDto>>

    @GET("dataset/api/checklist/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getByIdDataset(@Path("id") id: String): Response<ChecklistDto>

    @POST("dataset/api/checklist")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveDataset(@Body checklist: ChecklistDto): Response<ChecklistDto>
} 