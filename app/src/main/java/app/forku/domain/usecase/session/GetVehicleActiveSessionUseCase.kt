package app.forku.domain.usecase.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.user.User
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.UserRepository

import javax.inject.Inject


class GetVehicleActiveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(vehicleId: String): VehicleSessionInfo? {
        val activeSession = sessionRepository.getActiveSessionForVehicle(vehicleId) ?: return null
        val operator = activeSession.userId?.let { userRepository.getUserById(it) }

        return VehicleSessionInfo(
            session = activeSession,
            operator = operator
        )
    }
}