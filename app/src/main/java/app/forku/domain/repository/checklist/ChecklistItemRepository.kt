package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistItem

interface ChecklistItemRepository {
    suspend fun getAllChecklistItems(): List<ChecklistItem>
    suspend fun getChecklistItemsByChecklistId(checklistId: String): List<ChecklistItem>
    suspend fun getChecklistItemById(id: String): ChecklistItem?
    suspend fun createChecklistItem(item: ChecklistItem): ChecklistItem
    suspend fun updateChecklistItem(id: String, item: ChecklistItem): ChecklistItem
    suspend fun deleteChecklistItem(id: String): Boolean
} 