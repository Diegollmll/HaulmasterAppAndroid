package app.forku.data.mapper

import app.forku.data.dto.HazardIncidentDto
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun IncidentReportState.toHazardIncidentDto(): HazardIncidentDto {
    val hazardFields = typeSpecificFields as? IncidentTypeFields.HazardFields
        ?: IncidentTypeFields.HazardFields()

    // Formato seguro para SQL Server
    val currentDateTime = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // Si tienes incidentTime, úsalo para la hora
    val incidentDateTime = incidentTime?.let {
        LocalDateTime.now().withHour(it.hour).withMinute(it.minute).withSecond(0)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } ?: currentDateTime

    return HazardIncidentDto(
        correctiveActions = hazardFields.correctiveActions.map { it.ordinal },
        hazardType = hazardFields.hazardType?.ordinal,
        potentialConsequences = hazardFields.potentialConsequences.map { it.ordinal },
        preventiveMeasures = hazardFields.preventiveMeasures.map { it.ordinal },
        id = null, // Nuevo incidente
        description = description,
        userId = userId ?: "",
        incidentDateTime = incidentDateTime,
        incidentType = type?.ordinal ?: 0,
        locationDetails = locationDetails,
        severityLevel = severityLevel?.ordinal ?: 0,
        status = 0, // 0 = REPORTED
        weather = weather,
        isDirty = true,
        isNew = true,
        isMarkedForDeletion = false,
        vehicleId = vehicleId,
        locationCoordinates = locationCoordinates,
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeightEnum?.ordinal,
        othersInvolved = othersInvolved
    )
} 