package app.forku.data.repository.vehicle

import android.util.Log
import app.forku.data.api.VehicleTypeApi
import app.forku.data.api.dto.vehicle.VehicleTypeDto
import app.forku.data.api.dto.vehicle.toDto
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleTypeRepositoryImpl @Inject constructor(
    private val api: VehicleTypeApi
) : VehicleTypeRepository {

    override suspend fun getVehicleTypes(): List<VehicleType> {
        return try {
            Log.d("VehicleTypeRepo", "Fetching all vehicle types")
            val response = api.getAllVehicleTypes()
            if (!response.isSuccessful) {
                Log.e("VehicleTypeRepo", "Error fetching vehicle types: ${response.code()}")
                emptyList()
            } else {
                response.body()?.mapNotNull { dto ->
                    try {
                        dto.toModel()
                    } catch (e: Exception) {
                        Log.w("VehicleTypeRepo", "Error mapping vehicle type: ${dto.id}, Name: ${dto.name}, CategoryId: ${dto.categoryId}", e)
                        null
                    }
                } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("VehicleTypeRepo", "Error fetching vehicle types", e)
            emptyList()
        }
    }

    override suspend fun getVehicleTypeById(id: String): VehicleType {
        Log.d("VehicleTypeRepo", "Fetching vehicle type: $id")
        val response = api.getVehicleTypeById(id)
        if (!response.isSuccessful) {
            Log.e("VehicleTypeRepo", "Error fetching vehicle type: ${response.code()}")
            throw Exception("Failed to get vehicle type")
        }
        return response.body()?.toModel() ?: throw Exception("Vehicle type not found")
    }

    override suspend fun getVehicleTypesByCategory(categoryId: String): List<VehicleType> {
        return try {
            Log.d("VehicleTypeRepo", "Fetching vehicle types for category: $categoryId")
            val response = api.getVehicleTypesByCategory(categoryId)
            Log.d("VehicleTypeRepo", "Response code: ${response.code()}")
            
            if (!response.isSuccessful) {
                Log.e("VehicleTypeRepo", "Error fetching vehicle types: ${response.code()}")
                Log.e("VehicleTypeRepo", "Error body: ${response.errorBody()?.string()}")
                emptyList()
            } else {
                val types = response.body()?.map { it.toModel() } ?: emptyList()
                Log.d("VehicleTypeRepo", "Mapped vehicle types: $types")
                types
            }
        } catch (e: Exception) {
            Log.e("VehicleTypeRepo", "Error fetching vehicle types", e)
            emptyList()
        }
    }

    override suspend fun createVehicleType(
        name: String,
        categoryId: String,
        requiresCertification: Boolean
    ): VehicleType {
        Log.d("VehicleTypeRepo", "Creating vehicle type: $name")
        val response = api.saveVehicleType(
            VehicleTypeDto(
                id = "",  // ID will be assigned by the server
                name = name,
                categoryId = categoryId,
                requiresCertification = requiresCertification,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        if (!response.isSuccessful) {
            Log.e("VehicleTypeRepo", "Error creating vehicle type: ${response.code()}")
            throw Exception("Failed to create vehicle type")
        }
        return response.body()?.toModel() ?: throw Exception("Failed to create vehicle type")
    }

    override suspend fun updateVehicleType(
        id: String,
        name: String,
        categoryId: String,
        requiresCertification: Boolean
    ): VehicleType {
        Log.d("VehicleTypeRepo", "Updating vehicle type: $id")
        val response = api.saveVehicleType(
            VehicleTypeDto(
                id = id,
                name = name,
                categoryId = categoryId,
                requiresCertification = requiresCertification,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        if (!response.isSuccessful) {
            Log.e("VehicleTypeRepo", "Error updating vehicle type: ${response.code()}")
            throw Exception("Failed to update vehicle type")
        }
        return response.body()?.toModel() ?: throw Exception("Failed to update vehicle type")
    }

    override suspend fun deleteVehicleType(id: String) {
        Log.d("VehicleTypeRepo", "Deleting vehicle type: $id")
        val response = api.deleteVehicleType(id)
        if (!response.isSuccessful) {
            Log.e("VehicleTypeRepo", "Error deleting vehicle type: ${response.code()}")
            throw Exception("Failed to delete vehicle type")
        }
    }
} 
