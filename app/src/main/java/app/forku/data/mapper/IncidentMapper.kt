package app.forku.data.mapper

import app.forku.data.api.dto.incident.IncidentDto
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentStatus
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.incident.IncidentSeverityLevelEnum
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.net.Uri
import app.forku.data.api.dto.incident.TypeSpecificFieldsDto
import app.forku.domain.model.incident.LoadWeight

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
        locationDetails = locationDetails,
        weather = weather,
        incidentDateTime = incidentTime?.format(DateTimeFormatter.ISO_TIME),
        severityLevel = severityLevel?.name,
        preshiftCheckStatus = preshiftCheckStatus,
        typeSpecificFields = typeSpecificFields?.toDto() ?: TypeSpecificFieldsDto(
            type = type.name,
            data = emptyMap()
        ),
        othersInvolved = othersInvolved,
        injuries = injuries,
        injuryLocations = injuryLocations,
        vehicleType = vehicleType?.Name,
        vehicleName = vehicleName,
        checkId = checkId,
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeight?.name,
        locationCoordinates = locationCoordinates,
        photos = photos.map { it.toString() }
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
        locationDetails = locationDetails,
        weather = weather,
        incidentTime = incidentDateTime?.let { LocalTime.parse(it) },
        severityLevel = severityLevel?.let { IncidentSeverityLevelEnum.valueOf(it) },
        preshiftCheckStatus = preshiftCheckStatus,
        typeSpecificFields = typeSpecificFields.toDomain(),
        othersInvolved = othersInvolved,
        injuries = injuries,
        injuryLocations = injuryLocations,
        vehicleType = vehicleType?.let { type ->
            VehicleType(
                Id = type,
                Name = type,
                RequiresCertification = false,
                VehicleCategoryId = "",
                IsMarkedForDeletion = false,
                InternalObjectId = 0
            )
        },
        vehicleName = vehicleName,
        checkId = checkId,
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeight?.let { LoadWeight.valueOf(it) },
        locationCoordinates = locationCoordinates
    )
} 