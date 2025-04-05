package app.forku.domain.repository.vehicle

interface VehicleStatusChecker {
    /**
     * Check if a vehicle is available for use
     * @param vehicleId The ID of the vehicle to check
     * @param businessId The business context for the check
     * @return true if the vehicle is available, false otherwise
     */
    suspend fun isVehicleAvailable(vehicleId: String, businessId: String): Boolean

    /**
     * Get error message if vehicle is not available
     * @param vehicleId The ID of the vehicle to check
     * @param businessId The business context for the check
     * @return Error message if vehicle is not available, null otherwise
     */
    suspend fun getVehicleErrorMessage(vehicleId: String, businessId: String): String?
} 