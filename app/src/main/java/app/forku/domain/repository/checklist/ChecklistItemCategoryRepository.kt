package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistItemCategory

interface ChecklistItemCategoryRepository {
    suspend fun getAllCategories(): List<ChecklistItemCategory>
    suspend fun getCategoryById(id: String): ChecklistItemCategory?
} 