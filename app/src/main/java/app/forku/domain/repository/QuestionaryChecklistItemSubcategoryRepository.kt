package app.forku.domain.repository

import app.forku.data.model.QuestionaryChecklistItemSubcategoryDto

interface QuestionaryChecklistItemSubcategoryRepository {
    suspend fun getAllSubcategories(categoryId: String): List<QuestionaryChecklistItemSubcategoryDto>
    suspend fun getSubcategoryById(categoryId: String, id: String): QuestionaryChecklistItemSubcategoryDto
    suspend fun createSubcategory(categoryId: String, subcategory: QuestionaryChecklistItemSubcategoryDto): QuestionaryChecklistItemSubcategoryDto
    suspend fun updateSubcategory(categoryId: String, id: String, subcategory: QuestionaryChecklistItemSubcategoryDto): QuestionaryChecklistItemSubcategoryDto
    suspend fun deleteSubcategory(categoryId: String, id: String)
} 