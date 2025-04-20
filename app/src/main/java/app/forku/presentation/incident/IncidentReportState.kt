package app.forku.presentation.incident

import app.forku.domain.model.incident.IncidentType
import android.net.Uri
import app.forku.presentation.incident.model.IncidentFormSection
import app.forku.domain.model.incident.IncidentTypeFields
import com.google.android.gms.common.api.ResolvableApiException
import java.time.LocalTime
import app.forku.domain.model.vehicle.VehicleType
import java.time.LocalDateTime
import app.forku.domain.model.incident.IncidentSeverityLevelEnum
import app.forku.domain.model.incident.LoadWeight
import app.forku.domain.model.vehicle.Vehicle


data class IncidentReportState(
    // Common fields
    val type: IncidentType? = null,
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val locationDetails: String = "",
    val weather: String = "",
    val description: String = "",
    
    // Add new common fields
    val incidentTime: LocalTime? = null,
    val severityLevel: IncidentSeverityLevelEnum? = null,
    val preshiftCheckStatus: String = "",
    
    // Type-specific fields wrapper
    val typeSpecificFields: IncidentTypeFields? = null,
    
    // Session info
    val sessionId: String? = null,
    
    // People involved
    val userId: String? = null,
    val reporterName: String? = null,
    val othersInvolved: List<String> = emptyList(),
    val injuries: String = "",
    val injuryLocations: List<String> = emptyList(),
    
    // Vehicle info
    val vehicleId: String? = null,
    val vehicleType: VehicleType? = null,
    val vehicleName: String = "",
    val checkId: String? = null,
    val isLoadCarried: Boolean = false,
    val loadBeingCarried: String = "",
    val loadWeight: LoadWeight? = null,
    val lastPreshiftCheck: LocalDateTime? = null,
    
    // Incident specific
    val immediateActions: List<String> = emptyList(),
    val proposedSolutions: List<String> = emptyList(),
    val photos: List<Uri> = emptyList(),
    
    // Form state
    val currentSection: IncidentFormSection = IncidentFormSection.BasicInfo,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false,
    val attemptedSubmit: Boolean = false,
    
    // Location permission state
    val hasLocationPermission: Boolean = false,
    val locationCoordinates: String? = null,
    
    // Location settings state
    val showLocationSettingsDialog: Boolean = false,
    val locationSettingsException: ResolvableApiException? = null,
    
    // Navigation
    val navigateToDashboard: Boolean = false,
    
    // Add to IncidentReportState
    val availableVehicles: List<Vehicle> = emptyList(),
    val showVehicleSelector: Boolean = false,

    // Weather and location loading state
    val weatherLoaded: Boolean = false,
    val locationLoaded: Boolean = false
)

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

fun IncidentReportState.validate(): ValidationResult {
    return when (type) {
        IncidentType.COLLISION -> validateCollision()
        IncidentType.NEAR_MISS -> validateNearMiss()
        IncidentType.HAZARD -> validateHazard()
        IncidentType.VEHICLE_FAIL -> validateVehicleFail()
        else -> ValidationResult.Error("Invalid incident type")
    }
}

fun IncidentReportState.validateCollision(): ValidationResult {
    val fields = typeSpecificFields as? IncidentTypeFields.CollisionFields
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        vehicleId == null -> ValidationResult.Error("Vehicle information is required")
        userId == null -> ValidationResult.Error("Operator information is required")
        fields?.collisionType == null -> ValidationResult.Error("Collision type is required")
        fields.damageOccurrence == null -> ValidationResult.Error("Damage occurrence is required")
        fields.immediateCause == null -> ValidationResult.Error("Immediate cause is required")
        else -> ValidationResult.Success
    }
}

fun IncidentReportState.validateNearMiss(): ValidationResult {
    val fields = typeSpecificFields as? IncidentTypeFields.NearMissFields
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        userId == null -> ValidationResult.Error("Operator information is required")
        fields?.nearMissType == null -> ValidationResult.Error("Near miss type is required")
        fields.immediateCause == null -> ValidationResult.Error("Immediate cause is required")
        else -> ValidationResult.Success
    }
}

fun IncidentReportState.validateHazard(): ValidationResult {
    val fields = typeSpecificFields as? IncidentTypeFields.HazardFields
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        userId == null -> ValidationResult.Error("Operator information is required")
        fields?.hazardType == null -> ValidationResult.Error("Hazard type is required")
        else -> ValidationResult.Success
    }
}

fun IncidentReportState.validateVehicleFail(): ValidationResult {
    val fields = typeSpecificFields as? IncidentTypeFields.VehicleFailFields
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        vehicleId == null -> ValidationResult.Error("Vehicle information is required")
        userId == null -> ValidationResult.Error("Operator information is required")
        fields?.failureType == null -> ValidationResult.Error("Failure type is required")
        fields.damageOccurrence == null -> ValidationResult.Error("Damage occurrence is required")
        fields.immediateCause == null -> ValidationResult.Error("Immediate cause is required")
        else -> ValidationResult.Success
    }
}
