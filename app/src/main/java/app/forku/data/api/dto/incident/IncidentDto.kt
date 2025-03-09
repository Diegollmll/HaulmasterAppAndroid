package app.forku.data.api.dto.incident


import kotlinx.serialization.Serializable

@Serializable
data class IncidentDto(
    val id: String? = null,
    val type: String,
    val description: String,
    val timestamp: String,
    val userId: String,
    val status: String,
    val photoUrls: List<String>,
    val date: Long,
    val location: String,
    val locationDetails: String,
    val weather: String,
    val incidentTime: String?,
    val severityLevel: String?,
    val preshiftCheckStatus: String,
    
    // Vehicle info with consolidated load fields
    val vehicleId: String?,
    val vehicleType: String?,
    val vehicleName: String,
    val isLoadCarried: Boolean,
    val loadBeingCarried: String,
    val loadWeight: String?,
    
    // People involved
    val operatorId: String?,
    val othersInvolved: List<String>,
    val injuries: String,
    val injuryLocations: List<String>,
    
    // Type-specific fields
    val typeSpecificFields: TypeSpecificFieldsDto,

    // Additional fields
    val photos: List<String>,
    val locationCoordinates: String?,
    val sessionId: String?
)
