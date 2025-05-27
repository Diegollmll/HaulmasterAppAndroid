package app.forku.domain.repository.incident

import app.forku.data.api.dto.incident.IncidentMultimediaDto

interface IncidentMultimediaRepository {
    suspend fun addIncidentMultimedia(entityJson: String): Result<app.forku.data.api.dto.incident.IncidentMultimediaDto>
    suspend fun getIncidentMultimediaByIncidentId(incidentId: String): Result<List<IncidentMultimediaDto>>
} 