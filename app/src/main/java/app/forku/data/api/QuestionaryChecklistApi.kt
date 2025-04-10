package app.forku.data.api

import app.forku.data.model.QuestionaryChecklistDto
import retrofit2.Response
import retrofit2.http.*

interface QuestionaryChecklistApi {
    @GET("questionary-checklist")
    suspend fun getAllQuestionaries(): Response<List<QuestionaryChecklistDto>>

    @GET("questionary-checklist/{id}")
    suspend fun getQuestionaryById(@Path("id") id: String): Response<QuestionaryChecklistDto>

    @POST("questionary-checklist")
    suspend fun createQuestionary(@Body questionary: QuestionaryChecklistDto): Response<QuestionaryChecklistDto>

    @PUT("questionary-checklist/{id}")
    suspend fun updateQuestionary(
        @Path("id") id: String,
        @Body questionary: QuestionaryChecklistDto
    ): Response<QuestionaryChecklistDto>

    @DELETE("questionary-checklist/{id}")
    suspend fun deleteQuestionary(@Path("id") id: String): Response<Unit>
} 