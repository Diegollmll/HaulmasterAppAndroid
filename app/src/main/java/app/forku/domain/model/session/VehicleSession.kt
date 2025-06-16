package app.forku.domain.model.session

data class VehicleSession(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val checkId: String,
    val startTime: String,
    val endTime: String?,
    val status: VehicleSessionStatus,
    val startLocationCoordinates: String?,
    val endLocationCoordinates: String?,
    val durationMinutes: Int?,
    val timestamp: String,
    val closeMethod: VehicleSessionClosedMethod?,
    val closedBy: String? = null,
    val notes: String? = null,
    val operatorName: String = "Unknown",
    val vehicleName: String = "Unknown",
    val businessId: String? = null,
    val siteId: String? = null // ✅ Add siteId for multitenancy
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