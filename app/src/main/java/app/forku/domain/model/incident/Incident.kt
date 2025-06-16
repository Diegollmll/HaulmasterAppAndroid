package app.forku.domain.model.incident

import android.net.Uri
import app.forku.domain.model.vehicle.VehicleType
import java.time.LocalTime

data class Incident(
    val id: String? = null,
    val type: IncidentTypeEnum,
    val description: String,
    val timestamp: String,
    val userId: String,

    // Vehicle info
    val vehicleId: String?,
    val vehicleType: VehicleType?,
    val vehicleName: String,
    val checkId: String? = null, // ID of the associated preshift check
    // Nuevos campos consolidados de carga
    val isLoadCarried: Boolean = false,
    val loadBeingCarried: String = "",
    val loadWeight: LoadWeightEnum? = null,

    val sessionId: String? = null,
    val status: IncidentStatus = IncidentStatus.REPORTED,
    val photos: List<Uri> = emptyList(),
    
    // Additional fields from IncidentReportState
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val locationDetails: String = "",
    val weather: String = "",
    val incidentTime: LocalTime? = null,
    val severityLevel: IncidentSeverityLevelEnum? = null,
    val preshiftCheckStatus: String = "",
    val typeSpecificFields: IncidentTypeFields? = null,
    val othersInvolved: String? = null,
    val injuries: String = "",
    val injuryLocations: List<String> = emptyList(),
    val locationCoordinates: String? = null,
    
    // Business context for multitenancy
    val businessId: String? = null,
    val siteId: String? = null, // ✅ Add siteId for multitenancy
    
    // Creator info (included from API response)
    val creatorName: String = "Unknown"
) 