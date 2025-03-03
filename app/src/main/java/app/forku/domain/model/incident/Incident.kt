package app.forku.domain.model.incident

import android.net.Uri
import app.forku.domain.model.vehicle.VehicleType
import java.time.LocalTime

data class Incident(
    val id: String? = null,
    val type: IncidentType,
    val description: String,
    val timestamp: String,
    val userId: String,
    val vehicleId: String? = null,
    val sessionId: String? = null,
    val status: IncidentStatus = IncidentStatus.REPORTED,
    val photos: List<Uri> = emptyList(),
    
    // Additional fields from IncidentReportState
    val date: Long = System.currentTimeMillis(),
    val location: String = "",
    val locationDetails: String = "",
    val weather: String = "",
    val incidentTime: LocalTime? = null,
    val severityLevel: IncidentSeverityLevel? = null,
    val preshiftCheckStatus: String = "",
    val typeSpecificFields: IncidentTypeFields? = null,
    val operatorId: String? = null,
    val othersInvolved: List<String> = emptyList(),
    val injuries: String = "",
    val injuryLocations: List<String> = emptyList(),
    val vehicleType: VehicleType? = null,
    val vehicleName: String = "",
    val locationCoordinates: String? = null
) 