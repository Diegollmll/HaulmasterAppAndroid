package app.forku.domain.service

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleValidationService {
    suspend fun getVehicleStatus(vehicleId: String): VehicleStatus
    suspend fun isVehicleAvailable(vehicleId: String): Boolean
    suspend fun getVehicleErrorMessage(vehicleId: String): String?
    suspend fun validateVehicleForOperation(vehicleId: String)
    fun determineStatusFromCheck(checkStatus: String): VehicleStatus
} 