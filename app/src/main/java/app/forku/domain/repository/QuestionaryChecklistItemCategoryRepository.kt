package app.forku.domain.repository

import app.forku.data.api.dto.QuestionaryChecklistItemCategoryDto

interface QuestionaryChecklistItemCategoryRepository {
    suspend fun getAllCategories(): List<QuestionaryChecklistItemCategoryDto>
    suspend fun getCategoryById(id: String): QuestionaryChecklistItemCategoryDto
    suspend fun createCategory(category: QuestionaryChecklistItemCategoryDto): QuestionaryChecklistItemCategoryDto
    suspend fun updateCategory(id: String, category: QuestionaryChecklistItemCategoryDto): QuestionaryChecklistItemCategoryDto
    suspend fun deleteCategory(id: String)
} 