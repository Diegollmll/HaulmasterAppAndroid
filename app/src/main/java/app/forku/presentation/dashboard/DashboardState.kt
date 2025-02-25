package app.forku.presentation.dashboard

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.vehicle.VehicleStatus

data class DashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vehicle: Vehicle? = null,
    val lastPreShiftCheck: PreShiftCheck? = null,
    val isAuthenticated: Boolean = false,
    val showQrScanner: Boolean = false,
    val vehicleStatus: VehicleStatus = VehicleStatus.CHECKED_OUT
) 