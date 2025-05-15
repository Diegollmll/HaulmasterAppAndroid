package app.forku.domain.usecase.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject
import android.util.Log

class StartVehicleSessionUseCase @Inject constructor(
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(vehicleId: String, checkId: String): Result<VehicleSession> {
        val currentUser = userRepository.getCurrentUser()
            ?: return Result.failure(Exception("User not logged in"))

        Log.d("StartVehicleSessionUseCase", "Current user: $currentUser, role: ${currentUser.role}")
        Log.d("StartVehicleSessionUseCase", "vehicleId: $vehicleId, checkId: $checkId")

        // Allow both OPERATOR and ADMIN
        if (currentUser.role != UserRole.OPERATOR && currentUser.role != UserRole.ADMIN) {
            Log.e("StartVehicleSessionUseCase", "User does not have permission to operate vehicles. Role: ${currentUser.role}")
            return Result.failure(Exception("User does not have permission to operate vehicles"))
        }

        // Continuar con la lógica de inicio de sesión
        return try {
            val session = vehicleSessionRepository.startSession(vehicleId, checkId)
            Log.d("StartVehicleSessionUseCase", "Session started successfully: $session")
            Result.success(session)
        } catch (e: Exception) {
            Log.e("StartVehicleSessionUseCase", "Failed to start session: ${e.message}", e)
            Result.failure(e)
        }
    }
}