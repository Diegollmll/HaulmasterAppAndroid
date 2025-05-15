package app.forku.domain.usecase.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionClosedMethod
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject

class EndVehicleSessionUseCase @Inject constructor(
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        closeMethod: VehicleSessionClosedMethod,
        notes: String? = null
    ): Result<VehicleSession> {
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        return try {
            val session = vehicleSessionRepository.endSession(
                sessionId = sessionId,
                closeMethod = closeMethod,
                adminId = if (closeMethod == VehicleSessionClosedMethod.ADMIN_CLOSED) currentUser.id else null,
                notes = notes
            )
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 