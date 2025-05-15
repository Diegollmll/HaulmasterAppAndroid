package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistAnswerDto
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface ChecklistAnswerApi {
    @GET("api/checklistanswer/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(@Path("id") id: String): Response<ChecklistAnswerDto>

    @GET("api/checklistanswer/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getList(): Response<List<ChecklistAnswerDto>>

    @FormUrlEncoded
    @POST("api/checklistanswer")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun save(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") saveUpdateDto: String,
        @Field("include") include: String = ""
    ): Response<ChecklistAnswerDto>

    @DELETE("api/checklistanswer/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun delete(@Path("id") id: String): Response<Unit>
} 