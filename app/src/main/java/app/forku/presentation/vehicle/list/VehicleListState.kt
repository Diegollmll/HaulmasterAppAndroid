package app.forku.presentation.vehicle.list

import app.forku.domain.model.Vehicle

data class VehicleListState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 