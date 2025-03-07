package app.forku.domain.usecase.vehicle

import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.usecase.session.GetVehicleActiveSessionUseCase
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckByVehicleUseCase
import javax.inject.Inject
import app.forku.domain.repository.vehicle.VehicleStatusRepository

class GetVehicleStatusUseCase @Inject constructor(
    private val vehicleStatusRepository: VehicleStatusRepository
) {
    suspend operator fun invoke(vehicleId: String): VehicleStatus {
        return try {
            vehicleStatusRepository.getVehicleStatus(vehicleId)
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error getting vehicle status", e)
            VehicleStatus.AVAILABLE
        }
    }
} 