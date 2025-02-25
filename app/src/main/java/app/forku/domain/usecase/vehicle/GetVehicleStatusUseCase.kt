package app.forku.domain.usecase.vehicle


import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.AuthRepository

import javax.inject.Inject

class GetVehicleStatusUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): VehicleStatus {
        return try {
            val currentUser = authRepository.getCurrentUser()
            
            // First check if there's an active session
            val currentSession = sessionRepository.getCurrentSession()
            if (currentSession?.status == SessionStatus.ACTIVE) {
                // Check if session belongs to current user
                if (currentSession.userId == currentUser?.id) {
                    return VehicleStatus.IN_USE
                }
                return VehicleStatus.BLOCKED
            }

            // If no active session, check the last pre-shift check
            val lastCheck = vehicleRepository.getLastPreShiftCheck()
            return when (lastCheck?.status) {
                PreShiftStatus.COMPLETED_PASS.toString() -> VehicleStatus.CHECKED_IN
                PreShiftStatus.COMPLETED_FAIL.toString() -> VehicleStatus.BLOCKED
                PreShiftStatus.IN_PROGRESS.toString() -> VehicleStatus.CHECKED_IN
                PreShiftStatus.EXPIRED.toString() -> VehicleStatus.CHECKED_OUT
                null -> VehicleStatus.CHECKED_OUT
                else -> VehicleStatus.CHECKED_OUT
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error getting vehicle status", e)
            VehicleStatus.CHECKED_OUT
        }
    }
} 