package app.forku.data.api

import app.forku.data.api.dto.checklist.QuestionaryChecklistRotationRulesDto
import retrofit2.Response
import retrofit2.http.*

interface QuestionaryChecklistRotationRulesApi {
    @GET("dataset/api/questionarychecklistrotationrules/byid/{id}")
    suspend fun getByIdDataset(@Path("id") id: String): Response<QuestionaryChecklistRotationRulesDto>

    @GET("api/questionarychecklistrotationrules/byid/{id}")
    suspend fun getById(@Path("id") id: String): Response<QuestionaryChecklistRotationRulesDto>

    @GET("dataset/api/questionarychecklistrotationrules/list")
    suspend fun getListDataset(): Response<List<QuestionaryChecklistRotationRulesDto>>

    @GET("api/questionarychecklistrotationrules/list")
    suspend fun getList(): Response<List<QuestionaryChecklistRotationRulesDto>>

    @GET("dataset/api/questionarychecklistrotationrules/count")
    suspend fun getCount(): Response<Int>

    @POST("dataset/api/questionarychecklistrotationrules")
    suspend fun save(@Body rules: QuestionaryChecklistRotationRulesDto): Response<QuestionaryChecklistRotationRulesDto>

    @DELETE("dataset/api/questionarychecklistrotationrules")
    suspend fun delete(@Body rules: QuestionaryChecklistRotationRulesDto): Response<Unit>
} 