package app.forku.domain.model.session

data class VehicleSession(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val startTime: String,
    val endTime: String?,
    val status: SessionStatus,
    val startLocation: String?,
    val endLocation: String?,
    val durationMinutes: Int?,
    val timestamp: String
)

enum class SessionStatus {
    ACTIVE,
    INACTIVE
} 