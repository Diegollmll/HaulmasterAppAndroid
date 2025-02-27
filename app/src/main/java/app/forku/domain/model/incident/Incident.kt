package app.forku.domain.model.incident

import java.time.Instant

data class Incident(
    val id: String? = null,
    val type: IncidentType,
    val description: String,
    val timestamp: String,
    val userId: String,
    val vehicleId: String? = null,
    val sessionId: String? = null,
    val status: IncidentStatus = IncidentStatus.REPORTED
) 