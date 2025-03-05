package app.forku.domain.repository.incident

import app.forku.domain.model.incident.Incident

interface IncidentRepository {
    suspend fun reportIncident(incident: Incident): Result<Incident>
    suspend fun getIncidents(): Result<List<Incident>>
    suspend fun getIncidentById(id: String): Result<Incident>
    suspend fun getOperatorIncidents(): Result<List<Incident>>
} 