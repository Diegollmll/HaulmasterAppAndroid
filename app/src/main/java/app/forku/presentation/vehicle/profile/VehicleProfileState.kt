package app.forku.presentation.vehicle.profile

import app.forku.domain.model.Vehicle

data class VehicleProfileState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 