package app.forku.data.repository.vehicle

import app.forku.data.api.GeneralApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import javax.inject.Inject


class VehicleStatusUpdaterImpl @Inject constructor(
    private val api: GeneralApi
) : VehicleStatusUpdater {
    override suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Boolean {
        return try {
            val response = api.getVehicle(vehicleId)
            val vehicle = response.body()?.toDomain() 
                ?: throw Exception("Vehicle not found")
            
            val updatedVehicle = vehicle.copy(status = status)
            
            val updateResponse = api.updateVehicle(
                id = vehicleId,
                vehicle = updatedVehicle.toDto()
            )
            
            updateResponse.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error updating vehicle status", e)
            false
        }
    }
} 