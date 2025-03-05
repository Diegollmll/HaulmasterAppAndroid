package app.forku.presentation.user.profile

import app.forku.domain.model.user.Operator
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.Vehicle


data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val operator: Operator? = null,
    val activeVehicle: Vehicle? = null,
    val expiredQualifications: Int = 0
) 