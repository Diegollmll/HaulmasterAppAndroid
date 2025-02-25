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

data class StartSessionRequestDto(
    val vehicleId: String,
    val checkId: String,
    val timestamp: String
)

data class EndSessionRequestDto(
    val timestamp: String,
    val notes: String?
) 