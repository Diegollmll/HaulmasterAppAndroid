package app.forku.presentation.vehicle.profile

import app.forku.domain.model.vehicle.Vehicle

data class VehicleProfileState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showQrCode: Boolean = false
) 