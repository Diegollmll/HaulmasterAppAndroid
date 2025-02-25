package app.forku.domain.model.session

data class VehicleSession(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val startTime: String,
    val endTime: String? = null,
    val status: SessionStatus,
    val startLocation: String? = null,
    val endLocation: String? = null,
    val durationMinutes: Int? = null
)

enum class SessionStatus {
    ACTIVE,
    INACTIVE
} 