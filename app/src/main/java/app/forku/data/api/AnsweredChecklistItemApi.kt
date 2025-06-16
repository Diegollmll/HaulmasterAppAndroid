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

    @FormUrlEncoded
    @POST("api/answeredchecklistitem")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: application/json, text/javascript, */*; q=0.01"
    )
    suspend fun save(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String,
        @Field("include") include: String = "",
        @Field("dateformat") dateformat: String = "ISO8601",
        @Query("businessId") businessId: String? = null
    ): Response<AnsweredChecklistItemDto>

    @DELETE("dataset/api/answeredchecklistitem")
    suspend fun delete(@Body item: AnsweredChecklistItemDto): Response<Unit>
} 