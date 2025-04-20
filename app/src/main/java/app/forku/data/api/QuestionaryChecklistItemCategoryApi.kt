package app.forku.data.api

import app.forku.data.api.dto.QuestionaryChecklistItemCategoryDto
import retrofit2.Response
import retrofit2.http.*

interface QuestionaryChecklistItemCategoryApi {
    @GET("questionary-checklist-item-category")
    suspend fun getAllCategories(): Response<List<QuestionaryChecklistItemCategoryDto>>

    @GET("questionary-checklist-item-category/{id}")
    suspend fun getCategoryById(@Path("id") id: String): Response<QuestionaryChecklistItemCategoryDto>

    @POST("questionary-checklist-item-category")
    suspend fun createCategory(@Body category: QuestionaryChecklistItemCategoryDto): Response<QuestionaryChecklistItemCategoryDto>

    @PUT("questionary-checklist-item-category/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body category: QuestionaryChecklistItemCategoryDto
    ): Response<QuestionaryChecklistItemCategoryDto>

    @DELETE("questionary-checklist-item-category/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>
} 