package app.forku.data.api.dto.session

data class VehicleSessionDto(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val checkId: String,
    val startTime: String,
    val endTime: String?,
    val status: String,
    val startLocationCoordinates: String?,
    val endLocationCoordinates: String?,
    val timestamp: String,
    val closeMethod: String? = null,
    val closedBy: String? = null,
    val notes: String? = null
)