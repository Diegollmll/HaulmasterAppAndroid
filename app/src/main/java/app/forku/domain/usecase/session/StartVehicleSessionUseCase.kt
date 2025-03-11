package app.forku.domain.usecase.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.user.Permissions
import app.forku.domain.model.user.hasPermission
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject

class StartVehicleSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(vehicleId: String, checkId: String): Result<VehicleSession> {
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        if (!currentUser.hasPermission(Permissions.OPERATE_VEHICLE)) {
            return Result.failure(Exception("User does not have permission to operate vehicles"))
        }

        // Continuar con la lógica de inicio de sesión
        return try {
            val session = sessionRepository.startSession(vehicleId, checkId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}