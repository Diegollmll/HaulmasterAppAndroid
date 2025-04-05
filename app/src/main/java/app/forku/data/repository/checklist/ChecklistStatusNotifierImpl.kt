package app.forku.data.repository.checklist

import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.service.VehicleValidationService
import app.forku.domain.service.VehicleStatusDeterminer
import app.forku.data.datastore.AuthDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistStatusNotifierImpl @Inject constructor(
    private val vehicleStatusUpdater: VehicleStatusUpdater,
    private val vehicleStatusDeterminer: VehicleStatusDeterminer,
    private val authDataStore: AuthDataStore
) : ChecklistStatusNotifier {
    override suspend fun notifyCheckStatusChanged(vehicleId: String, checkStatus: String) {
        val businessId = authDataStore.getCurrentUser()?.businessId
            ?: throw Exception("User not authenticated or missing business ID")
            
        val newVehicleStatus = vehicleStatusDeterminer.determineStatusFromCheck(checkStatus)
        vehicleStatusUpdater.updateVehicleStatus(
            vehicleId = vehicleId,
            status = newVehicleStatus,
            businessId = businessId
        )
    }
} 