package app.forku.data.repository.incident

import app.forku.data.api.IncidentMultimediaApi
import app.forku.data.api.dto.incident.IncidentMultimediaDto
import app.forku.domain.repository.incident.IncidentMultimediaRepository
import javax.inject.Inject
import app.forku.core.business.BusinessContextManager

class IncidentMultimediaRepositoryImpl @Inject constructor(
    private val api: IncidentMultimediaApi,
    private val businessContextManager: BusinessContextManager
) : IncidentMultimediaRepository {
    override suspend fun addIncidentMultimedia(entityJson: String): Result<app.forku.data.api.dto.incident.IncidentMultimediaDto> = runCatching {
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        android.util.Log.d("IncidentMultimediaRepo", "Saving multimedia with businessId: $businessId, siteId: $siteId, entity: $entityJson")
        val response = api.saveIncidentMultimedia(entityJson, businessId, siteId)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response")
        } else {
            throw Exception("Failed to add incident multimedia: ${response.code()}")
        }
    }

    override suspend fun getIncidentMultimediaByIncidentId(incidentId: String): Result<List<IncidentMultimediaDto>> = runCatching {
        val response = api.getIncidentMultimediaByIncidentId(incidentId)
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get incident multimedia: ${response.code()}")
        }
    }
} 