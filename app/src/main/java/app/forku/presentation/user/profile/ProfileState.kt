package app.forku.presentation.user.profile

import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.session.VehicleSession

data class ProfileState(
    val user: User? = null,
    val currentSession: VehicleSession? = null,
    val activeVehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 