package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistChecklistItemCategory

interface ChecklistChecklistItemCategoryRepository {
    suspend fun getCategoriesByChecklistId(checklistId: String): List<ChecklistChecklistItemCategory>
    suspend fun getAllCategories(): List<ChecklistChecklistItemCategory>
    suspend fun getCategoryById(id: String): ChecklistChecklistItemCategory?
    suspend fun saveCategoryAssociation(category: ChecklistChecklistItemCategory): ChecklistChecklistItemCategory
    suspend fun deleteCategoryAssociation(id: String): Boolean
} 