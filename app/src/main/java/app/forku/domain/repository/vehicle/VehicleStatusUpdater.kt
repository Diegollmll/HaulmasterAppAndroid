package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleStatusUpdater {
    /**
     * Updates the status of a vehicle
     * @param vehicleId The ID of the vehicle to update
     * @param status The new status to set
     * @param businessId The business context for the update
     * @return true if update was successful, false otherwise
     */
    suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        businessId: String
    ): Boolean
} 