package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleStatusRepository : VehicleStatusChecker {
    suspend fun getVehicleStatus(vehicleId: String): VehicleStatus
    suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Boolean
    suspend fun determineStatusFromCheck(checkStatus: String): VehicleStatus
}