package app.forku.presentation.vehicle.profile

import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.Vehicle

data class VehicleProfileState(
    val vehicle: Vehicle? = null,
    val activeSession: VehicleSession? = null,
    val hasActiveSession: Boolean = false,
    val hasActivePreShiftCheck: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showQrCode: Boolean = false,
    val activeOperator: User? = null,
    val lastOperator: User? = null,
    val checkId: String? = null,
    val canStartCheck: Boolean = false,
    val navigateToChecklist: Boolean = false,
    val currentUserRole: UserRole? = null
) 