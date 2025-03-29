package app.forku.presentation.dashboard

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.user.User

data class DashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val lastPreShiftCheck: PreShiftCheck? = null,
    val isAuthenticated: Boolean = false,
    val showQrScanner: Boolean = false,
    val currentSession: VehicleSession? = null,
    val lastSession: VehicleSession? = null,
    val displayVehicle: Vehicle? = null,
    val navigationTarget: String? = null,
    val feedbackSubmitted: Boolean = false,
    val vehicles: List<Vehicle> = emptyList(),
    val activeSessions: List<VehicleSession> = emptyList(),
    val users: List<User> = emptyList(),
    val checks: List<PreShiftCheck> = emptyList()
)
//{
//    val displayVehicle: Vehicle?
//        get() = when {
//            currentSession != null -> getVehicleUseCase(currentSession.vehicleId)
//            lastPreShiftCheck != null -> getVehicleUseCase(lastPreShiftCheck.vehicleId)
//            else -> null
//        }
//}