package app.forku.data.service

import javax.inject.Inject
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.service.VehicleStatusDeterminer

class VehicleStatusDeterminerImpl @Inject constructor() : VehicleStatusDeterminer {
    override fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return when (checkStatus) {
            CheckStatus.COMPLETED_PASS.toString() -> VehicleStatus.AVAILABLE
            CheckStatus.COMPLETED_FAIL.toString() -> VehicleStatus.OUT_OF_SERVICE
            CheckStatus.IN_PROGRESS.toString() -> VehicleStatus.AVAILABLE
            CheckStatus.EXPIRED.toString() -> VehicleStatus.AVAILABLE
            else -> VehicleStatus.AVAILABLE
        }
    }
} 