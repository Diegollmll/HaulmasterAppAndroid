package app.forku.data.repository.checklist

import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.service.VehicleValidationService
import app.forku.domain.service.VehicleStatusDeterminer
import javax.inject.Inject

class ChecklistStatusNotifierImpl @Inject constructor(
    private val vehicleStatusUpdater: VehicleStatusUpdater,
    private val vehicleStatusDeterminer: VehicleStatusDeterminer
) : ChecklistStatusNotifier {
    override suspend fun notifyCheckStatusChanged(vehicleId: String, checkStatus: String) {
        val newVehicleStatus = vehicleStatusDeterminer.determineStatusFromCheck(checkStatus)
        vehicleStatusUpdater.updateVehicleStatus(vehicleId, newVehicleStatus)
    }
} 