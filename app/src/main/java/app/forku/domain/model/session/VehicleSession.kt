package app.forku.domain.model.session

data class VehicleSession(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val startTime: String,
    val endTime: String?,
    val status: VehicleSessionStatus,
    val startLocation: String?,
    val endLocation: String?,
    val durationMinutes: Int?,
    val timestamp: String,
    val closeMethod: VehicleSessionClosedMethod,
    val closedBy: String? = null
)

enum class VehicleSessionStatus {
    OPERATING, // A current online session
    NOT_OPERATING
}

enum class VehicleSessionClosedMethod {
    USER_CLOSED,      // When the operator ends their own session
    ADMIN_CLOSED,     // When an admin ends someone else's session
    TIMEOUT_CLOSED,   // When session times out due to inactivity
    GEOFENCE_CLOSED   // When session ends due to geofence violation
}