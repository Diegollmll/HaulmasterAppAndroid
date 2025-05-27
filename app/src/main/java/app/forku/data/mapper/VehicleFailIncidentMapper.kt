package app.forku.data.mapper

import app.forku.data.dto.VehicleFailIncidentDto
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
        environmentalImpact = fields?.environmentalImpact?.map { it },
        failImmediateCause = fields?.immediateCause?.let { listOf(it.ordinal) },
        failureType = fields?.failureType?.let { listOf(it.ordinal) } ?: emptyList(),
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeightEnum?.ordinal,
        longTermSolution = fields?.longTermSolutions?.map { it.ordinal },
        vehicleFailImmediateAction = fields?.immediateActions?.map { it.ordinal },
        vehicleId = vehicleId
    )
    return com.google.gson.Gson().toJson(dto)
} 