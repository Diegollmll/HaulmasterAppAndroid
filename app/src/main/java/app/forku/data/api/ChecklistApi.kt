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
    suspend fun getList(
        @Query("include") include: String? = null,
        @Query("filter") filter: String? = null,
        @Query("businessId") businessId: String? = null
    ): Response<List<ChecklistDto>>

    @GET("api/checklist/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getById(
        @Path("id") id: String,
        @Query("include") include: String? = null
    ): Response<ChecklistDto>

    @FormUrlEncoded
    @POST("api/checklist")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun save(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String,
        @Query("businessId") businessId: String? = null,
        @Query("include") include: String? = null
    ): Response<ChecklistDto>

    @GET("api/checklist/metadata")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getDefaultMetadata(): Response<ChecklistDefaultsDto>

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

data class ChecklistDefaultsDto(
    val defaultCriticalityLevels: List<Int> = listOf(0, 1),
    val defaultEnergySources: List<Int> = listOf(0, 1, 2),
    val defaultMaxQuestionsPerCheck: Int = 10,
    val defaultCriticalQuestionMinimum: Int = 3,
    val defaultStandardQuestionMaximum: Int = 5,
    val defaultRotationGroups: Int = 2
) 