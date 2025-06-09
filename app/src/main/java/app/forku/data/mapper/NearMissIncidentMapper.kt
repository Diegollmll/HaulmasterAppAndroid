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
    ).also {
        android.util.Log.d("NearMissIncidentMapper", "Mapping to NearMissIncidentDto. Weather field: $weather")
    }
}

fun NearMissIncidentDto.toTypeSpecificFields(): IncidentTypeFields.NearMissFields {
    return IncidentTypeFields.NearMissFields(
        nearMissType = this.nearMissType?.firstOrNull()?.let { NearMissType.values().getOrNull(it) },
        immediateCause = this.immediateCause?.firstOrNull()?.let { NearMissImmediateCause.values().getOrNull(it) },
        contributingFactors = this.contributingFactors?.mapNotNull { NearMissContributingFactor.values().getOrNull(it) }?.toSet() ?: emptySet(),
        immediateActions = this.immediateActions?.mapNotNull { NearMissImmediateAction.values().getOrNull(it) }?.toSet() ?: emptySet(),
        longTermSolutions = this.longTermSolutions?.mapNotNull { NearMissLongTermSolution.values().getOrNull(it) }?.toSet() ?: emptySet()
    )
}

fun NearMissIncidentDto.toDomain(): app.forku.domain.model.incident.Incident {
    return app.forku.domain.model.incident.Incident(
        id = id,
        type = app.forku.domain.model.incident.IncidentTypeEnum.NEAR_MISS,
        description = description,
        timestamp = incidentDateTime,
        userId = userId,
        vehicleId = vehicleId,
        vehicleType = null,
        vehicleName = "",
        checkId = null,
        isLoadCarried = isLoadCarried ?: false,
        loadBeingCarried = loadBeingCarried ?: "",
        loadWeight = null, // Map if available
        sessionId = null,
        status = app.forku.domain.model.incident.IncidentStatus.values().getOrNull(status) ?: app.forku.domain.model.incident.IncidentStatus.REPORTED,
        photos = emptyList(),
        date = try { java.time.ZonedDateTime.parse(incidentDateTime).toInstant().toEpochMilli() } catch (e: Exception) { 0L },
        location = locationDetails,
        locationDetails = locationDetails,
        weather = weather ?: "",
        incidentTime = null,
        severityLevel = app.forku.domain.model.incident.IncidentSeverityLevelEnum.values().getOrNull(severityLevel) ?: app.forku.domain.model.incident.IncidentSeverityLevelEnum.LOW,
        preshiftCheckStatus = "",
        typeSpecificFields = this.toTypeSpecificFields(),
        othersInvolved = othersInvolved,
        injuries = "",
        injuryLocations = emptyList(),
        locationCoordinates = locationCoordinates,
        creatorName = "Unknown" // This will be handled by the main IncidentDto mapper when using include=GOUser
    )
} 