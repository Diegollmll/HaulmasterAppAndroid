package app.forku.presentation.incident

import app.forku.domain.model.incident.IncidentType
import android.net.Uri
import app.forku.presentation.incident.model.IncidentFormSection
import app.forku.domain.model.incident.IncidentTypeFields
import com.google.android.gms.common.api.ResolvableApiException
import java.time.LocalTime
import app.forku.domain.model.vehicle.VehicleType
import java.time.LocalDateTime
import app.forku.domain.model.incident.IncidentSeverityLevel

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
    val severityLevel: IncidentSeverityLevel? = null,
    val preshiftCheckStatus: String = "",
    
    // Type-specific fields wrapper
    val typeSpecificFields: IncidentTypeFields? = null,
    
    // Session info
    val sessionId: String? = null,
    
    // People involved
    val operatorId: String? = null,
    val othersInvolved: List<String> = emptyList(),
    val injuries: String = "",
    val injuryLocations: List<String> = emptyList(),
    
    // Vehicle info
    val vehicleId: String? = null,
    val vehicleType: VehicleType? = null,
    val vehicleName: String = "",
    val loadBeingCarried: String = "",
    val loadWeight: String = "",
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
    
    // Location permission state
    val hasLocationPermission: Boolean = false,
    val locationCoordinates: String? = null,
    
    // Location settings state
    val showLocationSettingsDialog: Boolean = false,
    val locationSettingsException: ResolvableApiException? = null,
    
    // Navigation
    val navigateToDashboard: Boolean = false
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
        IncidentType.VEHICLE_FAIL -> validateVehicleFailure()
        else -> ValidationResult.Error("Invalid incident type")
    }
}

fun IncidentReportState.validateCollision(): ValidationResult {
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        location.isBlank() -> ValidationResult.Error("Location is required")
        vehicleId == null -> ValidationResult.Error("Vehicle information is required")
        operatorId == null -> ValidationResult.Error("Operator information is required")
        else -> ValidationResult.Success
    }
}

fun IncidentReportState.validateNearMiss(): ValidationResult {
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        location.isBlank() -> ValidationResult.Error("Location is required")
        else -> ValidationResult.Success
    }
}

fun IncidentReportState.validateHazard(): ValidationResult {
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        location.isBlank() -> ValidationResult.Error("Location is required")
        else -> ValidationResult.Success
    }
}

fun IncidentReportState.validateVehicleFailure(): ValidationResult {
    return when {
        description.isBlank() -> ValidationResult.Error("Description is required")
        vehicleId == null -> ValidationResult.Error("Vehicle information is required")
        location.isBlank() -> ValidationResult.Error("Location is required")
        else -> ValidationResult.Success
    }
}
