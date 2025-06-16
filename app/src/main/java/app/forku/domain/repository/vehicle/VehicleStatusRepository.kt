package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleStatusRepository : VehicleStatusChecker {
    suspend fun getVehicleStatus(vehicleId: String, businessId: String): VehicleStatus
    suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus, businessId: String, siteId: String? = null): Boolean
    suspend fun determineStatusFromCheck(checkStatus: String): VehicleStatus
}