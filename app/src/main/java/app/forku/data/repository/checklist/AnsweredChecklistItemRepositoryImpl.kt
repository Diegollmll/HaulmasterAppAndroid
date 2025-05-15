package app.forku.data.repository.checklist

import app.forku.data.api.AnsweredChecklistItemApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import javax.inject.Inject
import com.google.gson.Gson
import app.forku.core.auth.HeaderManager

class AnsweredChecklistItemRepositoryImpl @Inject constructor(
    private val api: AnsweredChecklistItemApi,
    private val gson: Gson,
    private val headerManager: HeaderManager
) : AnsweredChecklistItemRepository {
    override suspend fun getById(id: String): AnsweredChecklistItem? {
        return api.getById(id).body()?.toDomain()
    }

    override suspend fun getAll(): List<AnsweredChecklistItem> {
        return api.getList().body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun save(item: AnsweredChecklistItem): AnsweredChecklistItem {
        // Convert DTO to JSON string for the 'entity' field
        val dto = item.toDto()
        val jsonString = gson.toJson(dto)
        android.util.Log.d("AnsweredChecklistItemRepository", "JSON enviado a API: $jsonString")

        // Get CSRF token and cookie from headers
        val headers = headerManager.getHeaders().getOrThrow()
        val csrfToken = headers.csrfToken
        val cookie = headers.cookie

        // Call the API with the new signature (headers first, then fields)
        val response = api.save(
            csrfToken = csrfToken,
            cookie = cookie,
            entity = jsonString,
            include = "",
            dateformat = "ISO8601"
        )
        return response.body()?.toDomain()
            ?: throw Exception("Failed to save AnsweredChecklistItem")
    }

    override suspend fun delete(item: AnsweredChecklistItem) {
        api.delete(item.toDto())
    }
} 