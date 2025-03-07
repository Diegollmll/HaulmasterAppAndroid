package app.forku.data.repository.vehicle

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.checklist.ChecklistRepository


class VehicleRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore,
    private val validateChecklistUseCase: ValidateChecklistUseCase
) : VehicleRepository {

    override suspend fun getVehicle(id: String): Vehicle {
        val response = api.getVehicle(id)
        return response.body()?.toDomain()
            ?: throw Exception("Vehicle not found")
    }

    override suspend fun getVehicleByQr(code: String): Vehicle {
        val response = api.getVehicleByQr(code)
        return response.body()?.toDomain()
            ?: throw Exception("Vehicle not found")
    }

    override suspend fun getVehicles(): List<Vehicle> {
        try {
            val response = api.getVehicles()
            android.util.Log.d("VehicleRepo", "Raw API response: ${response.body()}")
            return response.body()?.map { it.toDomain() }
                ?: throw Exception("Failed to get vehicles: Empty response body")
        } catch (e: Exception) {
            android.util.Log.e("VehicleRepo", "Error fetching vehicles", e)
            throw e
        }
    }

    override suspend fun getVehicleStatus(vehicleId: String): VehicleStatus {
        // Implementation remains the same, but use VehicleStatus directly instead of through UseCase
        throw UnsupportedOperationException("Method not implemented")
    }

    override suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Vehicle {
        try {
            // First get the current vehicle
            val response = api.getVehicle(vehicleId)
            val vehicle = response.body()?.toDomain() 
                ?: throw Exception("Vehicle not found")
            
            // Update only the status
            val updatedVehicle = vehicle.copy(status = status)
            
            // Make the PUT request with the updated vehicle
            val updateResponse = api.updateVehicle(
                id = vehicleId,
                vehicle = updatedVehicle.toDto()
            )
            
            if (!updateResponse.isSuccessful) {
                throw Exception("Failed to update vehicle status: ${updateResponse.code()}")
            }
            
            return updateResponse.body()?.toDomain() 
                ?: throw Exception("No vehicle data in response")
        } catch (e: Exception) {
            throw Exception("Error updating vehicle status: ${e.message}")
        }
    }

}