package app.forku.data.repository.vehicle

import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.domain.service.VehicleValidationService
import javax.inject.Inject

class VehicleStatusRepositoryImpl @Inject constructor(
    private val vehicleValidationService: VehicleValidationService,
    private val vehicleStatusUpdater: VehicleStatusUpdater
) : VehicleStatusRepository {

    override suspend fun getVehicleStatus(vehicleId: String): VehicleStatus {
        return vehicleValidationService.getVehicleStatus(vehicleId)
    }

    override suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Boolean {
        vehicleStatusUpdater.updateVehicleStatus(vehicleId, status)
        return true
    }

    override suspend fun isVehicleAvailable(vehicleId: String): Boolean {
        return vehicleValidationService.isVehicleAvailable(vehicleId)
    }

    override suspend fun getVehicleErrorMessage(vehicleId: String): String? {
        return vehicleValidationService.getVehicleErrorMessage(vehicleId)
    }

    override suspend fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return vehicleValidationService.determineStatusFromCheck(checkStatus)
    }
} 