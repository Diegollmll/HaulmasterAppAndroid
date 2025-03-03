package app.forku.data.api.dto.incident

import kotlinx.serialization.Serializable

@Serializable
data class IncidentDto(
    val id: String? = null,
    val type: String,
    val description: String,
    val timestamp: String,
    val userId: String,
    val vehicleId: String? = null,
    val sessionId: String? = null,
    val status: String,
    val photoUrls: List<String>,
    val date: Long,
    val location: String,
    val locationDetails: String,
    val weather: String,
    val incidentTime: String?,
    val severityLevel: String?,
    val preshiftCheckStatus: String,
    val typeSpecificFields: TypeSpecificFieldsDto?,
    val operatorId: String?,
    val othersInvolved: List<String>,
    val injuries: String,
    val injuryLocations: List<String>,
    val vehicleType: String?,
    val vehicleName: String,
    val locationCoordinates: String?
)

@Serializable
data class TypeSpecificFieldsDto(
    val type: String,
    val data: Map<String, String>
)