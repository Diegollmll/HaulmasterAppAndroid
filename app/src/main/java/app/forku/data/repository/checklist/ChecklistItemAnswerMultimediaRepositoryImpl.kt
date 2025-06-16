package app.forku.data.repository.checklist

import app.forku.data.api.ChecklistItemAnswerMultimediaApi
import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto
import app.forku.domain.repository.checklist.ChecklistItemAnswerMultimediaRepository
import javax.inject.Inject
import java.net.URLEncoder
import app.forku.core.auth.HeaderManager
import app.forku.core.business.BusinessContextManager

class ChecklistItemAnswerMultimediaRepositoryImpl @Inject constructor(
    private val api: ChecklistItemAnswerMultimediaApi,
    private val headerManager: HeaderManager,
    private val businessContextManager: BusinessContextManager
) : ChecklistItemAnswerMultimediaRepository {
    override suspend fun addChecklistItemAnswerMultimedia(entityJson: String): Result<ChecklistItemAnswerMultimediaDto> = runCatching {
        val businessId = businessContextManager.getCurrentBusinessId()
        val dto = com.google.gson.Gson().fromJson(entityJson, app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto::class.java)
        val dtoWithBusiness = dto.copy(businessId = businessId)
        val jsonWithBusiness = com.google.gson.Gson().toJson(dtoWithBusiness)
        android.util.Log.d("ChecklistMultimediaRepo", "Saving multimedia with businessId: $businessId, entity: $jsonWithBusiness")
        val response = api.save(jsonWithBusiness, businessId)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response")
        } else {
            throw Exception("Failed to add checklist item answer multimedia: ${response.code()}")
        }
    }

    override suspend fun getChecklistItemAnswerMultimediaByAnswerId(answerId: String): Result<List<ChecklistItemAnswerMultimediaDto>> = runCatching {
        val filter = "AnsweredChecklistItemId==Guid.Parse(\"$answerId\")"
        android.util.Log.d("ChecklistImage", "[Repo] Raw filter: $filter")
        val headers = headerManager.getHeaders().getOrThrow()
        val csrfToken = headers.csrfToken
        val cookie = headers.cookie
        android.util.Log.d("ChecklistImage", "[Repo] Using CSRF token: $csrfToken, Cookie: $cookie")
        val response = api.getAll(
            filter,
            accept = "text/plain",
            csrfToken = csrfToken,
            cookie = cookie
        )
        android.util.Log.d("ChecklistImage", "[Repo] Response code: ${response.code()}")
        android.util.Log.d("ChecklistImage", "[Repo] Response body: ${response.body()}")
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch multimedia: ${response.code()}")
        }
    }

    override suspend fun deleteChecklistItemAnswerMultimedia(id: String): Result<Unit> = runCatching {
        val response = api.delete(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete checklist item answer multimedia: ${response.code()}")
        }
    }
} 