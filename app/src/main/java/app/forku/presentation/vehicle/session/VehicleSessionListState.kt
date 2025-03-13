package app.forku.presentation.vehicle.session

import app.forku.domain.model.vehicle.Vehicle
import app.forku.presentation.dashboard.VehicleSessionInfo

data class VehicleSessionListState(
    val vehicles: List<VehicleWithSessionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

data class VehicleWithSessionInfo(
    val vehicle: Vehicle,
    val activeSession: VehicleSessionInfo?,
    val preShiftStatus: String
) 