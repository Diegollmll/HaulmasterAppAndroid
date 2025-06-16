package app.forku.data.api

import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto
import retrofit2.Response
import retrofit2.http.*

interface ChecklistItemAnswerMultimediaApi {
    @GET("api/checklistitemanswermultimedia/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<ChecklistItemAnswerMultimediaDto>

    @GET("api/checklistitemanswermultimedia/list")
    suspend fun getAll(
        @Query("filter") filter: String,
        @Header("Accept") accept: String = "text/plain",
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<List<ChecklistItemAnswerMultimediaDto>>

    @FormUrlEncoded
    @POST("api/checklistitemanswermultimedia")
    @Headers("Accept: text/plain")
    suspend fun save(
        @Field("entity") entity: String,
        @Query("businessId") businessId: String? = null
    ): Response<ChecklistItemAnswerMultimediaDto>

    @DELETE("api/checklistitemanswermultimedia/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
} 