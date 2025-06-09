package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.VehicleWithRelatedData

interface VehicleRepository {
    /**
     * Gets a specific vehicle by ID
     */
    suspend fun getVehicle(id: String, businessId: String): Vehicle
    
    /**
     * Gets a vehicle with optimized included data (sessions, checklists) in single API call
     * @param id The vehicle ID
     * @param businessId The business context
     */
    suspend fun getVehicleWithOptimizedData(id: String, businessId: String): app.forku.data.mapper.VehicleWithSessionAndOperatorData
    
    /**
     * Gets vehicles for the current business and site context
     * @param businessId The ID of the business
     * @param siteId Optional site ID to filter vehicles by site
     * @param includeRelatedData Whether to include related data (sessions, checklist answers)
     */
    suspend fun getVehicles(businessId: String, siteId: String? = null, includeRelatedData: Boolean = false): List<Vehicle>
    
    /**
     * Gets vehicles with related data optimized for list views (single API call)
     * @param businessId The ID of the business
     * @param siteId Optional site ID to filter vehicles by site
     */
    suspend fun getVehiclesWithRelatedData(businessId: String, siteId: String? = null): List<VehicleWithRelatedData>
    
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

    /**
     * Creates a new vehicle
     * @param codename The vehicle's codename
     * @param model The vehicle's model
     * @param type The vehicle's type
     * @param description The vehicle's description
     * @param bestSuitedFor What the vehicle is best suited for
     * @param photoModel The vehicle's photo model
     * @param energyType The vehicle's energy type
     * @param nextService The vehicle's next service date
     * @param businessId The business context
     * @param serialNumber The vehicle's serial number
     */
    suspend fun createVehicle(
        codename: String,
        model: String,
        type: VehicleType,
        description: String,
        bestSuitedFor: String,
        photoModel: String,
        energyType: String,
        nextService: String,
        businessId: String?,
        serialNumber: String
    ): Vehicle

    /**
     * Updates vehicle information globally (SYSTEM_OWNER/SUPERADMIN)
     * @param vehicleId The ID of the vehicle to update
     * @param updatedVehicle The vehicle object with updated data
     */
    suspend fun updateVehicleGlobally(vehicleId: String, updatedVehicle: Vehicle): Vehicle

    /**
     * Updates vehicle information within a specific business context.
     * @param businessId The ID of the business the vehicle belongs to.
     * @param vehicleId The ID of the vehicle to update.
     * @param updatedVehicle The vehicle object with updated data.
     */
    suspend fun updateVehicle(businessId: String, vehicleId: String, updatedVehicle: Vehicle): Vehicle
}