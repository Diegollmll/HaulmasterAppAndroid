package app.forku.data.api

import app.forku.data.api.dto.checklist.QuestionaryChecklistMetadataDto
import retrofit2.Response
import retrofit2.http.*

interface QuestionaryChecklistMetadataApi {
    @GET("dataset/api/questionarychecklistmetadata/byid/{id}")
    suspend fun getByIdDataset(@Path("id") id: String): Response<QuestionaryChecklistMetadataDto>

    @GET("api/questionarychecklistmetadata/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<QuestionaryChecklistMetadataDto>

    @GET("dataset/api/questionarychecklistmetadata/list")
    suspend fun getListDataset(): Response<List<QuestionaryChecklistMetadataDto>>

    @GET("api/questionarychecklistmetadata/list")
    suspend fun getList(): Response<List<QuestionaryChecklistMetadataDto>>

    @GET("dataset/api/questionarychecklistmetadata/count")
    suspend fun getCount(): Response<Int>

    @POST("dataset/api/questionarychecklistmetadata")
    suspend fun save(@Body metadata: QuestionaryChecklistMetadataDto): Response<QuestionaryChecklistMetadataDto>

    @DELETE("dataset/api/questionarychecklistmetadata")
    suspend fun delete(@Body metadata: QuestionaryChecklistMetadataDto): Response<Unit>
} 