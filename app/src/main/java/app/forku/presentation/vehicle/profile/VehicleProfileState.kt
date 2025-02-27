package app.forku.presentation.vehicle.profile

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.session.VehicleSessionInfo

data class VehicleProfileState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showQrCode: Boolean = false,
    val activeSession: VehicleSessionInfo? = null,
    val hasActivePreShiftCheck: Boolean = false,
    val hasActiveSession: Boolean = false
) 