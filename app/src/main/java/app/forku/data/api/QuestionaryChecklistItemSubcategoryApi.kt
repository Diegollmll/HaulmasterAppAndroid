package app.forku.data.api

import app.forku.data.model.QuestionaryChecklistItemSubcategoryDto
import retrofit2.Response
import retrofit2.http.*

interface QuestionaryChecklistItemSubcategoryApi {
    @GET("questionary-checklist-item-subcategory")
    suspend fun getAllSubcategories(
        @Query("categoryId") categoryId: String
    ): Response<List<QuestionaryChecklistItemSubcategoryDto>>

    @GET("questionary-checklist-item-subcategory/{id}")
    suspend fun getSubcategoryById(
        @Path("id") id: String
    ): Response<QuestionaryChecklistItemSubcategoryDto>

    @POST("questionary-checklist-item-subcategory")
    suspend fun createSubcategory(
        @Body subcategory: QuestionaryChecklistItemSubcategoryDto
    ): Response<QuestionaryChecklistItemSubcategoryDto>

    @PUT("questionary-checklist-item-subcategory/{id}")
    suspend fun updateSubcategory(
        @Path("id") id: String,
        @Body subcategory: QuestionaryChecklistItemSubcategoryDto
    ): Response<QuestionaryChecklistItemSubcategoryDto>

    @DELETE("questionary-checklist-item-category/{categoryId}/questionary-checklist-item-subcategory/{subcategoryId}")
    suspend fun deleteSubcategory(
        @Path("categoryId") categoryId: String,
        @Path("subcategoryId") subcategoryId: String
    ): Response<Unit>

    @DELETE("questionary-checklist-item-subcategory/{id}")
    suspend fun deleteSubcategorySimple(
        @Path("id") id: String
    ): Response<Unit>
} 