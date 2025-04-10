package app.forku.data.api

import app.forku.data.model.QuestionaryChecklistItemDto
import retrofit2.Response
import retrofit2.http.*

interface QuestionaryChecklistItemApi {
    @GET("questionary-checklist/{checklistId}/questionary-checklist-item")
    suspend fun getItemsByQuestionaryChecklistId(
        @Path("checklistId") checklistId: String
    ): Response<List<QuestionaryChecklistItemDto>>

    @GET("questionary-checklist-item")
    suspend fun getAllItems(): Response<List<QuestionaryChecklistItemDto>>

    @GET("questionary-checklist-item/{id}")
    suspend fun getItemById(@Path("id") id: String): Response<QuestionaryChecklistItemDto>

    @POST("questionary-checklist-item")
    suspend fun createItem(@Body item: QuestionaryChecklistItemDto): Response<QuestionaryChecklistItemDto>

    @PUT("questionary-checklist-item/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Body item: QuestionaryChecklistItemDto
    ): Response<QuestionaryChecklistItemDto>

    @DELETE("questionary-checklist/{checklistId}/questionary-checklist-item/{id}")
    suspend fun deleteItem(
        @Path("checklistId") checklistId: String,
        @Path("id") id: String
    ): Response<Unit>
} 