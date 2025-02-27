package app.forku.data.mapper

import app.forku.data.api.dto.incident.IncidentRequestDto
import app.forku.data.api.dto.incident.IncidentResponseDto
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentStatus
import app.forku.domain.model.incident.IncidentType

fun Incident.toDto() = IncidentRequestDto(
    type = type.name,
    description = description,
    timestamp = timestamp,
    userId = userId,
    vehicleId = vehicleId,
    sessionId = sessionId
)

fun IncidentResponseDto.toDomain() = Incident(
    id = id,
    type = IncidentType.valueOf(type),
    description = description,
    timestamp = timestamp,
    userId = userId,
    vehicleId = vehicleId,
    sessionId = sessionId,
    status = IncidentStatus.valueOf(status)
) 