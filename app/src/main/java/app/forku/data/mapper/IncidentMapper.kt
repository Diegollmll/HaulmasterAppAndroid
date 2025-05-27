package app.forku.data.mapper

import app.forku.data.api.dto.incident.IncidentDto
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentStatus
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.incident.IncidentSeverityLevelEnum
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.net.Uri
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.domain.model.incident.LoadWeightEnum

fun Incident.toDto(): IncidentDto {
    return IncidentDto(
        id = id,
        description = description,
        userId = userId,
        incidentDateTime = incidentTime?.format(DateTimeFormatter.ISO_TIME) ?: "",
        incidentType = type.ordinal,
        locationDetails = locationDetails,
        severityLevel = severityLevel?.ordinal ?: 0,
        status = status.ordinal,
        isDirty = true,
        isNew = true,
        isMarkedForDeletion = false
    )
}

fun IncidentDto.toDomain(): Incident {
    return Incident(
        id = id ?: "",
        type = IncidentTypeEnum.values().getOrNull(incidentType) ?: IncidentTypeEnum.COLLISION,
        description = description,
        timestamp = "", // This will be set by the server
        userId = userId,
        status = IncidentStatus.values().getOrNull(status) ?: IncidentStatus.REPORTED,
        date = try {
            java.time.ZonedDateTime.parse(incidentDateTime).toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        },
        locationDetails = locationDetails,
        incidentTime = try {
            java.time.ZonedDateTime.parse(incidentDateTime).toLocalTime()
        } catch (e: Exception) {
            null
        },
        severityLevel = IncidentSeverityLevelEnum.values().getOrNull(severityLevel) ?: IncidentSeverityLevelEnum.LOW,
        preshiftCheckStatus = "", // This will be set by the server
        typeSpecificFields = null, // This will be handled separately if needed
        vehicleType = null,
        vehicleName = "",
        checkId = "",
        isLoadCarried = false,
        loadBeingCarried = "",
        loadWeight = null, // Map if available
        sessionId = "",
        photos = emptyList(),
        weather = "",
        vehicleId = ""
    )
} 