package app.forku.data.api.dto.session

data class SessionDto(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val startTime: String,
    val endTime: String?,
    val status: String,
    val startLocation: String?,
    val endLocation: String?
)