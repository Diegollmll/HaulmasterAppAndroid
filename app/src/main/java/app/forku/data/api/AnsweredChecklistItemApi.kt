package app.forku.data.api

import app.forku.data.api.dto.checklist.AnsweredChecklistItemDto
import retrofit2.Response
import retrofit2.http.*

interface AnsweredChecklistItemApi {
    @GET("dataset/api/answeredchecklistitem/byid/{id}")
    suspend fun getByIdDataset(@Path("id") id: String): Response<AnsweredChecklistItemDto>

    @GET("api/answeredchecklistitem/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<AnsweredChecklistItemDto>

    @GET("dataset/api/answeredchecklistitem/list")
    suspend fun getListDataset(): Response<List<AnsweredChecklistItemDto>>

    @GET("api/answeredchecklistitem/list")
    suspend fun getList(): Response<List<AnsweredChecklistItemDto>>

    @GET("dataset/api/answeredchecklistitem/count")
    suspend fun getCount(): Response<Int>

    @POST("dataset/api/answeredchecklistitem")
    suspend fun save(@Body item: AnsweredChecklistItemDto): Response<AnsweredChecklistItemDto>

    @DELETE("dataset/api/answeredchecklistitem")
    suspend fun delete(@Body item: AnsweredChecklistItemDto): Response<Unit>
} 