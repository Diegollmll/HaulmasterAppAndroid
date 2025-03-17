package app.forku.domain.model.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.Vehicle

data class VehicleSessionInfo(
    val vehicle: Vehicle,
    val session: VehicleSession,
    val operator: User?,
    val operatorName: String,
    val operatorImage: String?,
    val sessionStartTime: String?,
    val userRole: UserRole = UserRole.OPERATOR,
    val codename: String?,
    val vehicleImage: String?,
    val progress: Float?,
    val vehicleId: String?,
    val vehicleType: String?
) 