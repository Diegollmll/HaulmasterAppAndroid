package app.forku.data.mapper

import app.forku.data.dto.VehicleFailIncidentDto
import app.forku.domain.model.incident.DamageOccurrence
import app.forku.domain.model.incident.EnvironmentalImpact
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.LoadWeightEnum
import app.forku.domain.model.incident.VehicleFailContributingFactor
import app.forku.domain.model.incident.VehicleFailImmediateAction
import app.forku.domain.model.incident.VehicleFailImmediateCause
import app.forku.domain.model.incident.VehicleFailLongTermSolution
import app.forku.domain.model.incident.VehicleFailType
import app.forku.presentation.incident.IncidentReportState
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun IncidentReportState.toVehicleFailIncidentDto(): String {
    val fields = typeSpecificFields as? app.forku.domain.model.incident.IncidentTypeFields.VehicleFailFields

    val dto = app.forku.data.dto.VehicleFailIncidentDto(
        id = null, // New incident
        description = description,
        userId = userId ?: "",
        incidentDateTime = incidentTime?.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME) ?: java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME),
        incidentType = type?.ordinal ?: 0,
        locationDetails = locationDetails,
        severityLevel = severityLevel?.ordinal ?: 0,
        status = 0, // Default status for new incidents
        weather = weather,
        isDirty = true,
        isNew = true,
        isMarkedForDeletion = false,
        locationCoordinates = locationCoordinates,
        othersInvolved = othersInvolved,
        // Vehicle Fail specific fields
        contributingFactors = fields?.contributingFactors?.map { it.ordinal },
        damageOccurrence = fields?.damageOccurrence?.map { it.ordinal },
        environmentalImpact = fields?.environmentalImpact?.map { it.ordinal },
        failImmediateCause = fields?.immediateCause?.let { listOf(it.ordinal) },
        failureType = fields?.failureType?.let { listOf(it.ordinal) } ?: emptyList(),
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeightEnum?.ordinal,
        longTermSolution = fields?.longTermSolutions?.map { it.ordinal },
        vehicleFailImmediateAction = fields?.immediateActions?.map { it.ordinal },
        vehicleId = vehicleId
    )
    android.util.Log.d("VehicleFailIncidentMapper", "Mapping to VehicleFailIncidentDto. Weather field: $weather")
    return com.google.gson.Gson().toJson(dto)
}

fun VehicleFailIncidentDto.toTypeSpecificFields(): IncidentTypeFields.VehicleFailFields {
    return IncidentTypeFields.VehicleFailFields(
        failureType = this.failureType?.firstOrNull()?.let { VehicleFailType.values().getOrNull(it) },
        systemAffected = "", // Not present in DTO
        maintenanceHistory = "", // Not present in DTO
        operationalImpact = "", // Not present in DTO
        immediateCause = this.failImmediateCause?.firstOrNull()?.let { VehicleFailImmediateCause.values().getOrNull(it) },
        contributingFactors = this.contributingFactors?.mapNotNull { VehicleFailContributingFactor.values().getOrNull(it) }?.toSet() ?: emptySet(),
        immediateActions = this.vehicleFailImmediateAction?.mapNotNull { VehicleFailImmediateAction.values().getOrNull(it) }?.toSet() ?: emptySet(),
        longTermSolutions = this.longTermSolution?.mapNotNull { VehicleFailLongTermSolution.values().getOrNull(it) }?.toSet() ?: emptySet(),
        damageOccurrence = this.damageOccurrence?.mapNotNull { DamageOccurrence.values().getOrNull(it) }?.toSet() ?: emptySet(),
        environmentalImpact = this.environmentalImpact?.mapNotNull { EnvironmentalImpact.values().getOrNull(it) }?.toSet() ?: emptySet(),
        isLoadCarried = this.isLoadCarried ?: false,
        loadBeingCarried = this.loadBeingCarried ?: "",
        loadWeightEnum = this.loadWeight?.let { LoadWeightEnum.values().getOrNull(it) }
    )
}

fun VehicleFailIncidentDto.toDomain(): app.forku.domain.model.incident.Incident {
    return app.forku.domain.model.incident.Incident(
        id = id,
        type = app.forku.domain.model.incident.IncidentTypeEnum.VEHICLE_FAIL,
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