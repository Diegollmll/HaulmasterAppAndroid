package app.forku.data.mapper

import app.forku.data.dto.NearMissIncidentDto
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.NearMissType
import app.forku.domain.model.incident.*

fun IncidentReportState.toNearMissIncidentDto(): NearMissIncidentDto {
    val nearMissFields = typeSpecificFields as? IncidentTypeFields.NearMissFields
        ?: IncidentTypeFields.NearMissFields()

    // Formato seguro para SQL Server
    val currentDateTime = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // Si tienes incidentTime, úsalo para la hora
    val incidentDateTime = incidentTime?.let {
        java.time.LocalDateTime.now().withHour(it.hour).withMinute(it.minute).withSecond(0)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } ?: currentDateTime

    return NearMissIncidentDto(
        contributingFactors = nearMissFields.contributingFactors.map { it.ordinal },
        immediateActions = nearMissFields.immediateActions.map { it.ordinal },
        immediateCause = nearMissFields.immediateCause?.let { listOf(it.ordinal) } ?: emptyList(),
        isLoadCarried = isLoadCarried,
        loadWeight = loadWeightEnum?.ordinal,
        loadBeingCarried = loadBeingCarried,
        longTermSolutions = nearMissFields.longTermSolutions.map { it.ordinal },
        nearMissType = nearMissFields.nearMissType?.let { listOf(it.ordinal) } ?: emptyList(),
        othersInvolved = othersInvolved,
        vehicleId = vehicleId,
        // IncidentDto fields
        id = null, // Nuevo incidente
        description = description,
        userId = userId ?: "",
        incidentDateTime = incidentDateTime,
        incidentType = type?.ordinal ?: 0,
        locationCoordinates = locationCoordinates,
        locationDetails = locationDetails,
        severityLevel = severityLevel?.ordinal ?: 0,
        status = 0, // 0 = REPORTED
        weather = weather,
        isDirty = true,
        isNew = true,
        isMarkedForDeletion = false
    )
} 