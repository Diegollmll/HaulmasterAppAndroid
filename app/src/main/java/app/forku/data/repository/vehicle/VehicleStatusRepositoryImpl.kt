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

    override suspend fun getVehicleStatus(vehicleId: String, businessId: String): VehicleStatus {
        return vehicleValidationService.getVehicleStatus(vehicleId, businessId)
    }

    override suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        businessId: String
    ): Boolean {
        return vehicleStatusUpdater.updateVehicleStatus(vehicleId, status, businessId)
    }

    override suspend fun isVehicleAvailable(vehicleId: String, businessId: String): Boolean {
        return vehicleValidationService.isVehicleAvailable(vehicleId, businessId)
    }

    override suspend fun getVehicleErrorMessage(vehicleId: String, businessId: String): String? {
        return vehicleValidationService.getVehicleErrorMessage(vehicleId, businessId)
    }

    override suspend fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return vehicleValidationService.determineStatusFromCheck(checkStatus)
    }
} 