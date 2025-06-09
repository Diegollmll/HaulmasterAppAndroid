package app.forku.data.mapper

import app.forku.data.dto.CollisionIncidentDto
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.*
import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.domain.model.incident.IncidentStatus
import com.google.gson.Gson

fun IncidentReportState.toCollisionIncidentDto(): CollisionIncidentDto {
    val collisionFields = typeSpecificFields as? IncidentTypeFields.CollisionFields
        ?: IncidentTypeFields.CollisionFields()

    // Formato seguro para SQL Server
    val currentDateTime = java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    // Si tienes incidentTime, úsalo para la hora
    val incidentDateTime = incidentTime?.let {
        java.time.LocalDateTime.now().withHour(it.hour).withMinute(it.minute).withSecond(0)
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } ?: currentDateTime

    val dto = CollisionIncidentDto(
        // Campos específicos
        injurySeverity = collisionFields.injurySeverity.ordinal,
        injuryLocations = collisionFields.injuryLocations.mapNotNull { name ->
            try { InjuryLocation.valueOf(name).ordinal } catch (e: Exception) { null }
        },
        damageOccurrence = collisionFields.damageOccurrence.map { it.ordinal },
        collisionType = collisionFields.collisionType?.let { listOf(it.ordinal) } ?: emptyList(),
        commonCauses = collisionFields.commonCause?.let { listOf(it.ordinal) } ?: emptyList(),
        contributingFactors = collisionFields.contributingFactors.map { it.ordinal },
        immediateActions = collisionFields.immediateActions.map { it.ordinal },
        immediateCauses = collisionFields.immediateCause?.let { listOf(it.ordinal) } ?: emptyList(),
        longTermSolutions = collisionFields.longTermSolutions.map { it.ordinal },
        environmentalImpact = collisionFields.environmentalImpact.map { it.ordinal },
        // Campos base
        id = null, // Nuevo incidente
        description = description,
        userId = userId ?: "",
        incidentDateTime = incidentDateTime,
        incidentType = type?.ordinal ?: 0,
        locationDetails = locationDetails,
        severityLevel = severityLevel?.ordinal ?: 0,
        status = 0, // 0 = REPORTED
        isDirty = true,
        isNew = true,
        isMarkedForDeletion = false,
        vehicleId = vehicleId,
        locationCoordinates = locationCoordinates,
        weather = weather,
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeightEnum?.ordinal,
        othersInvolved = othersInvolved
    )

    // Log JSON para depuración
    try {
        val json = Gson().toJson(dto)
        android.util.Log.d("CollisionIncidentDto", "JSON enviado: $json")
    } catch (e: Exception) {
        android.util.Log.e("CollisionIncidentDto", "Error serializando JSON", e)
    }

    return dto
}

fun Incident.toCollisionIncidentDto(): CollisionIncidentDto {
    val collisionFields = typeSpecificFields as? IncidentTypeFields.CollisionFields
        ?: IncidentTypeFields.CollisionFields()

    return CollisionIncidentDto(
        id = null,
        incidentType = type.ordinal,
        description = description,
        locationDetails = locationDetails,
        incidentDateTime = incidentTime?.toString() ?: java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        severityLevel = severityLevel?.ordinal ?: 0,
        status = 0,
        vehicleId = vehicleId,
        userId = userId,
        locationCoordinates = locationCoordinates,
        weather = weather,
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeight?.ordinal,
        othersInvolved = othersInvolved,
        injurySeverity = collisionFields.injurySeverity.ordinal,
        injuryLocations = collisionFields.injuryLocations.mapNotNull { name ->
            try { InjuryLocation.valueOf(name).ordinal } catch (e: Exception) { null }
        },
        damageOccurrence = collisionFields.damageOccurrence.map { it.ordinal },
        collisionType = collisionFields.collisionType?.let { listOf(it.ordinal) } ?: emptyList(),
        commonCauses = collisionFields.commonCause?.let { listOf(it.ordinal) } ?: emptyList(),
        contributingFactors = collisionFields.contributingFactors.map { it.ordinal },
        immediateActions = collisionFields.immediateActions.map { it.ordinal },
        immediateCauses = collisionFields.immediateCause?.let { listOf(it.ordinal) } ?: emptyList(),
        longTermSolutions = collisionFields.longTermSolutions.map { it.ordinal },
        environmentalImpact = collisionFields.environmentalImpact.map { it.ordinal },
        isDirty = true,
        isNew = true,
        isMarkedForDeletion = false
    )
}

fun CollisionIncidentDto.toDomain(): Incident {
    android.util.Log.d("CollisionIncidentMapper", "Mapping CollisionIncidentDto to Incident. Weather field: ${weather}")
    return Incident(
        id = id,
        type = IncidentTypeEnum.COLLISION,
        description = description ?: "",
        timestamp = incidentDateTime ?: "",
        userId = userId ?: "",
        vehicleId = vehicleId,
        vehicleType = null, // Map if available
        vehicleName = "",   // Map if available
        checkId = null,
        isLoadCarried = isLoadCarried ?: false,
        loadBeingCarried = loadBeingCarried ?: "",
        loadWeight = null, // Map if available
        sessionId = null,
        status = IncidentStatus.values().getOrNull(status) ?: IncidentStatus.REPORTED,
        photos = emptyList(), // Map if available
        date = try { java.time.ZonedDateTime.parse(incidentDateTime).toInstant().toEpochMilli() } catch (e: Exception) { 0L },
        location = locationDetails,
        locationDetails = locationDetails,
        weather = weather ?: "",
        incidentTime = null, // Map if available
        severityLevel = IncidentSeverityLevelEnum.values().getOrNull(severityLevel) ?: IncidentSeverityLevelEnum.LOW,
        preshiftCheckStatus = "",
        typeSpecificFields = this.toTypeSpecificFields(),
        othersInvolved = othersInvolved ?: "",
        injuries = "",
        injuryLocations = emptyList(),
        locationCoordinates = locationCoordinates,
        creatorName = "Unknown" // This will be handled by the main IncidentDto mapper when using include=GOUser
    )
} 