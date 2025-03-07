package app.forku.domain.repository.vehicle

interface VehicleStatusChecker {
    suspend fun isVehicleAvailable(vehicleId: String): Boolean
    suspend fun getVehicleErrorMessage(vehicleId: String): String?
} 