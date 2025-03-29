package app.forku.presentation.session

import app.forku.domain.model.session.VehicleSession

data class SessionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionEnded: Boolean = false,
    val session: VehicleSession? = null
) 