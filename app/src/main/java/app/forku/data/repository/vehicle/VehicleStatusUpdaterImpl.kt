package app.forku.data.repository.vehicle

import app.forku.data.api.VehicleApi
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleStatusUpdaterImpl @Inject constructor(
    private val api: VehicleApi
) : VehicleStatusUpdater {
    override suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        businessId: String
    ): Boolean {
        return try {
            // Fetch the current vehicle
            val currentResponse = api.getVehicleById(vehicleId)
            if (!currentResponse.isSuccessful) {
                throw Exception("Failed to fetch vehicle: ${currentResponse.code()}")
            }
            val currentVehicleDto = currentResponse.body() ?: throw Exception("Vehicle not found")
            // Update the status (use property name, not serialized name)
            val updatedVehicleDto = currentVehicleDto.copy(status = status.name)
            // Save the updated vehicle
            val response = api.saveVehicle(updatedVehicleDto)
            if (!response.isSuccessful) {
                throw Exception("Failed to update vehicle status: ${response.code()}")
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error updating vehicle status", e)
            false
        }
    }
} 