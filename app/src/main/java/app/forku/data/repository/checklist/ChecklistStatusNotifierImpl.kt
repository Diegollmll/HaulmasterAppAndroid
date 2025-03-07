package app.forku.data.repository.checklist

import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.vehicle.VehicleStatus
import javax.inject.Inject

class ChecklistStatusNotifierImpl @Inject constructor(
    private val vehicleStatusUpdater: VehicleStatusUpdater
) : ChecklistStatusNotifier {
    override suspend fun notifyCheckStatusChanged(vehicleId: String, checkStatus: String) {
        val newVehicleStatus = determineStatusFromCheck(checkStatus)
        vehicleStatusUpdater.updateVehicleStatus(vehicleId, newVehicleStatus)
    }

    private fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return when (checkStatus) {
            CheckStatus.COMPLETED_PASS.toString() -> VehicleStatus.AVAILABLE
            CheckStatus.COMPLETED_FAIL.toString() -> VehicleStatus.OUT_OF_SERVICE
            CheckStatus.IN_PROGRESS.toString() -> VehicleStatus.AVAILABLE
            CheckStatus.EXPIRED.toString() -> VehicleStatus.AVAILABLE
            else -> VehicleStatus.AVAILABLE
        }
    }
} 