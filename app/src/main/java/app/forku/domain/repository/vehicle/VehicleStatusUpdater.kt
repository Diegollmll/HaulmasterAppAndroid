package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleStatusUpdater {
    suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Boolean
} 