package app.forku.presentation.session

import app.forku.domain.model.session.VehicleSession

data class SessionState(
    val session: VehicleSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 