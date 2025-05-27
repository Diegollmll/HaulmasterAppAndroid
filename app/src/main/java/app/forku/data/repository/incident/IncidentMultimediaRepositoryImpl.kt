package app.forku.data.repository.incident

import app.forku.data.api.IncidentMultimediaApi
import app.forku.data.api.dto.incident.IncidentMultimediaDto
import app.forku.domain.repository.incident.IncidentMultimediaRepository
import javax.inject.Inject

class IncidentMultimediaRepositoryImpl @Inject constructor(
    private val api: IncidentMultimediaApi
) : IncidentMultimediaRepository {
    override suspend fun addIncidentMultimedia(entityJson: String): Result<app.forku.data.api.dto.incident.IncidentMultimediaDto> = runCatching {
        val response = api.saveIncidentMultimedia(entityJson)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response")
        } else {
            throw Exception("Failed to add incident multimedia: \\${response.code()}")
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