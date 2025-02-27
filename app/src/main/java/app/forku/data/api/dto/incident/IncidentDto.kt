package app.forku.data.api.dto.incident

data class IncidentRequestDto(
    val type: String,
    val description: String,
    val timestamp: String,
    val userId: String,
    val vehicleId: String? = null,
    val sessionId: String? = null
)

data class IncidentResponseDto(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val userId: String,
    val vehicleId: String?,
    val sessionId: String?,
    val status: String
) 