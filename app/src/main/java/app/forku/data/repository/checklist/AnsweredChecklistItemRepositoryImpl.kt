package app.forku.data.repository.checklist

import app.forku.data.api.AnsweredChecklistItemApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import javax.inject.Inject

class AnsweredChecklistItemRepositoryImpl @Inject constructor(
    private val api: AnsweredChecklistItemApi
) : AnsweredChecklistItemRepository {
    override suspend fun getById(id: String): AnsweredChecklistItem? {
        return api.getById(id).body()?.toDomain()
    }

    override suspend fun getAll(): List<AnsweredChecklistItem> {
        return api.getList().body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun save(item: AnsweredChecklistItem): AnsweredChecklistItem {
        return api.save(item.toDto()).body()?.toDomain()
            ?: throw Exception("Failed to save AnsweredChecklistItem")
    }

    override suspend fun delete(item: AnsweredChecklistItem) {
        api.delete(item.toDto())
    }
} 