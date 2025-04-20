package app.forku.domain.repository

import app.forku.data.api.dto.QuestionaryChecklistItemDto

interface QuestionaryChecklistItemRepository {
    suspend fun getItemsByChecklistId(checklistId: String): List<QuestionaryChecklistItemDto>
    suspend fun getAllItems(): List<QuestionaryChecklistItemDto>
    suspend fun getItemById(id: String): QuestionaryChecklistItemDto
    suspend fun createItem(item: QuestionaryChecklistItemDto): QuestionaryChecklistItemDto
    suspend fun updateItem(id: String, item: QuestionaryChecklistItemDto): QuestionaryChecklistItemDto
    suspend fun deleteItem(checklistId: String, id: String)
} 