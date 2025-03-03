package app.forku.data.mapper

import app.forku.data.api.dto.incident.IncidentDto
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentStatus
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.incident.IncidentSeverityLevel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.net.Uri

fun Incident.toDto(): IncidentDto {
    return IncidentDto(
        id = id,
        type = type.name,
        description = description,
        timestamp = timestamp,
        userId = userId,
        vehicleId = vehicleId,
        sessionId = sessionId,
        status = status.name,
        photoUrls = photos.map { it.toString() },
        date = date,
        location = location,
        locationDetails = locationDetails,
        weather = weather,
        incidentTime = incidentTime?.format(DateTimeFormatter.ISO_TIME),
        severityLevel = severityLevel?.name,
        preshiftCheckStatus = preshiftCheckStatus,
        typeSpecificFields = typeSpecificFields?.toDto(),
        operatorId = operatorId,
        othersInvolved = othersInvolved,
        injuries = injuries,
        injuryLocations = injuryLocations,
        vehicleType = vehicleType?.name,
        vehicleName = vehicleName,
        locationCoordinates = locationCoordinates
    )
}

fun IncidentDto.toDomain(): Incident {
    return Incident(
        id = id,
        type = IncidentType.valueOf(type),
        description = description,
        timestamp = timestamp,
        userId = userId,
        vehicleId = vehicleId,
        sessionId = sessionId,
        status = IncidentStatus.valueOf(status),
        photos = photoUrls.map { Uri.parse(it) },
        date = date,
        location = location,
        locationDetails = locationDetails,
        weather = weather,
        incidentTime = incidentTime?.let { LocalTime.parse(it) },
        severityLevel = severityLevel?.let { IncidentSeverityLevel.valueOf(it) },
        preshiftCheckStatus = preshiftCheckStatus,
        typeSpecificFields = typeSpecificFields?.toDomain(),
        operatorId = operatorId,
        othersInvolved = othersInvolved,
        injuries = injuries,
        injuryLocations = injuryLocations,
        vehicleType = vehicleType?.let { VehicleType.valueOf(it) },
        vehicleName = vehicleName,
        locationCoordinates = locationCoordinates
    )
} 