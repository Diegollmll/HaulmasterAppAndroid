package app.forku.presentation.vehicle.list

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.User
import app.forku.domain.model.checklist.PreShiftCheck

data class VehicleListState(
    val vehicles: List<Vehicle> = emptyList(),
    val vehicleSessions: Map<String, VehicleSessionInfo> = emptyMap(),
    val lastPreShiftChecks: Map<String, PreShiftCheck?> = emptyMap(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentBusinessId: String? = null,
    val hasBusinessContext: Boolean = false,
    val availableSites: List<app.forku.domain.model.Site> = emptyList(),
    val selectedSiteId: String? = null
)
