package app.forku.data.mapper

import app.forku.data.dto.HazardIncidentDto
import app.forku.domain.model.incident.HazardConsequence
import app.forku.domain.model.incident.HazardCorrectiveAction
import app.forku.domain.model.incident.HazardPreventiveMeasure
import app.forku.domain.model.incident.HazardType
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
    ).also {
        android.util.Log.d("HazardIncidentMapper", "Mapping to HazardIncidentDto. Weather field: $weather")
    }
}

fun HazardIncidentDto.toTypeSpecificFields(): IncidentTypeFields.HazardFields {
    return IncidentTypeFields.HazardFields(
        hazardType = this.hazardType?.let { HazardType.values().getOrNull(it) },
        potentialConsequences = this.potentialConsequences?.mapNotNull { HazardConsequence.values().getOrNull(it) }?.toSet() ?: emptySet(),
        correctiveActions = this.correctiveActions?.mapNotNull { HazardCorrectiveAction.values().getOrNull(it) }?.toSet() ?: emptySet(),
        preventiveMeasures = this.preventiveMeasures?.mapNotNull { HazardPreventiveMeasure.values().getOrNull(it) }?.toSet() ?: emptySet()
    )
}

fun HazardIncidentDto.toDomain(): app.forku.domain.model.incident.Incident {
    return app.forku.domain.model.incident.Incident(
        id = id,
        type = app.forku.domain.model.incident.IncidentTypeEnum.HAZARD,
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
        creatorName = "Unknown", // This will be handled by the main IncidentDto mapper when using include=GOUser
        businessId = businessId,
        siteId = siteId // ✅ Include siteId from DTO
    )
} 