package app.forku.domain.usecase.vehicle

import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.session.GetVehicleActiveSessionUseCase
import javax.inject.Inject

class GetVehicleStatusUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val getVehicleActiveSessionUseCase: GetVehicleActiveSessionUseCase
) {
    suspend operator fun invoke(vehicleId: String): VehicleStatus {
        return try {
            // First check if there's an active session
            val activeSession = getVehicleActiveSessionUseCase(vehicleId)
            if (activeSession?.session?.status == SessionStatus.ACTIVE) {
                return VehicleStatus.IN_USE
            }

            // If no active session, check the last pre-shift check
            val lastCheck = vehicleRepository.getLastPreShiftCheck(vehicleId)
            return when (lastCheck?.status) {
                PreShiftStatus.COMPLETED_PASS.toString() -> VehicleStatus.AVAILABLE
                PreShiftStatus.COMPLETED_FAIL.toString() -> VehicleStatus.BLOCKED
                PreShiftStatus.IN_PROGRESS.toString() -> VehicleStatus.BLOCKED
                PreShiftStatus.EXPIRED.toString() -> VehicleStatus.AVAILABLE
                null -> VehicleStatus.UNKNOWN
                else -> VehicleStatus.UNKNOWN
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error getting vehicle status", e)
            VehicleStatus.UNKNOWN
        }
    }
} 