package app.forku.data.repository.checklist

import app.forku.data.api.AnsweredChecklistItemApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import javax.inject.Inject
import com.google.gson.Gson
import app.forku.core.auth.HeaderManager
import app.forku.core.business.BusinessContextManager

class AnsweredChecklistItemRepositoryImpl @Inject constructor(
    private val api: AnsweredChecklistItemApi,
    private val gson: Gson,
    private val headerManager: HeaderManager,
    private val businessContextManager: BusinessContextManager
) : AnsweredChecklistItemRepository {
    override suspend fun getById(id: String): AnsweredChecklistItem? {
        return api.getById(id).body()?.toDomain()
    }

    override suspend fun getAll(): List<AnsweredChecklistItem> {
        return api.getList().body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun save(item: AnsweredChecklistItem): AnsweredChecklistItem {
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        val dto = item.toDto().copy(businessId = businessId, siteId = siteId)
        val entityJson = gson.toJson(dto)
        val headers = headerManager.getHeaders().getOrThrow()
        val csrfToken = headers.csrfToken
        val cookie = headers.cookie
        val response = api.save(
            csrfToken = csrfToken,
            cookie = cookie,
            entity = entityJson,
            businessId = businessId
        )
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Failed to save AnsweredChecklistItem: ${response.code()}")
        }
        return response.body()!!.toDomain()
    }

    override suspend fun delete(item: AnsweredChecklistItem) {
        api.delete(item.toDto())
    }
} 