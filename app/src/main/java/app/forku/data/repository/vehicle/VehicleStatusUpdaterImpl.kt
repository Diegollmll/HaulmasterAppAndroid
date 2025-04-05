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
            val response = api.updateVehicleStatus(
                businessId = businessId,
                vehicleId = vehicleId,
                status = status.name
            )
            
            if (!response.isSuccessful) {
                when (response.code()) {
                    404 -> throw Exception("Vehicle not found")
                    429 -> throw Exception("Rate limit exceeded. Please try again later.")
                    in 500..599 -> throw Exception("Server error. Please try again later.")
                    else -> throw Exception("Failed to update vehicle status: ${response.code()}")
                }
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error updating vehicle status", e)
            false
        }
    }
} 