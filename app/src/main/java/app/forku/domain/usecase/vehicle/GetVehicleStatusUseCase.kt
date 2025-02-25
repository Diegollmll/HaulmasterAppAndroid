package app.forku.domain.usecase.vehicle

import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.AuthRepository
import javax.inject.Inject

class GetVehicleStatusUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): VehicleStatus {
        return try {
            val currentUser = authRepository.getCurrentUser()
            
            // Primero verificamos si hay una sesión activa
            val currentSession = vehicleRepository.getCurrentSession()
            if (currentSession?.status == SessionStatus.ACTIVE) {
                // Verificar si la sesión pertenece al usuario actual
                if (currentSession.userId == currentUser?.id) {
                    return VehicleStatus.IN_USE
                }
                return VehicleStatus.BLOCKED
            }

            // Si no hay sesión activa, verificamos el último check
            val lastCheck = vehicleRepository.getLastPreShiftCheck()
            return when (lastCheck?.status) {
                PreShiftStatus.COMPLETED_PASS.toString() -> VehicleStatus.CHECKED_IN
                PreShiftStatus.COMPLETED_FAIL.toString() -> VehicleStatus.BLOCKED
                null -> VehicleStatus.CHECKED_OUT
                else -> VehicleStatus.CHECKED_OUT
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error getting vehicle status", e)
            VehicleStatus.CHECKED_OUT
        }
    }
} 