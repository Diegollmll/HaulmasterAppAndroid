package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleRepository {
    /**
     * Gets a specific vehicle by ID
     */
    suspend fun getVehicle(id: String, businessId: String): Vehicle
    
    /**
     * Gets vehicles for the current business and site context
     * @param businessId The ID of the business
     * @param siteId Optional site ID to filter vehicles by site
     */
    suspend fun getVehicles(businessId: String, siteId: String? = null): List<Vehicle>
    
    /**
     * Gets all vehicles across all businesses (SuperAdmin only)
     */
    suspend fun getAllVehicles(): List<Vehicle>
    
    /**
     * Gets a vehicle by QR code
     * @param code The QR code
     * @param checkAvailability Whether to check if the vehicle is available
     * @param businessId The business context for availability check
     */
    suspend fun getVehicleByQr(
        code: String, 
        checkAvailability: Boolean = true,
        businessId: String? = null
    ): Vehicle

    /**
     * Updates vehicle status
     * @param vehicleId The ID of the vehicle
     * @param status The new status
     * @param businessId The business context
     */
    suspend fun updateVehicleStatus(
        vehicleId: String, 
        status: VehicleStatus,
        businessId: String
    ): Vehicle

    /**
     * Gets vehicle status
     * @param vehicleId The ID of the vehicle
     * @param businessId The business context
     */
    suspend fun getVehicleStatus(
        vehicleId: String,
        businessId: String
    ): VehicleStatus
}