package app.forku.domain.model.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.user.User

data class VehicleSessionInfo(
    val session: VehicleSession,
    val operator: User?
) 