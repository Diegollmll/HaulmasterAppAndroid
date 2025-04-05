package app.forku.domain.service

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleValidationService {
    /**
     * Get the current status of a vehicle
     * @param vehicleId The ID of the vehicle to check
     * @param businessId The business context for the check
     * @return The current status of the vehicle
     */
    suspend fun getVehicleStatus(vehicleId: String, businessId: String): VehicleStatus

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

    /**
     * Validate if a vehicle can be used for operation
     * @param vehicleId The ID of the vehicle to validate
     * @param businessId The business context for the validation
     * @throws Exception if vehicle is not available
     */
    suspend fun validateVehicleForOperation(vehicleId: String, businessId: String)

    /**
     * Determine vehicle status from check status
     * @param checkStatus The check status to evaluate
     * @return The determined vehicle status
     */
    fun determineStatusFromCheck(checkStatus: String): VehicleStatus
} 