package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.AnsweredChecklistItem

interface AnsweredChecklistItemRepository {
    suspend fun getById(id: String): AnsweredChecklistItem?
    suspend fun getAll(): List<AnsweredChecklistItem>
    suspend fun save(item: AnsweredChecklistItem): AnsweredChecklistItem
    suspend fun delete(item: AnsweredChecklistItem)
} 