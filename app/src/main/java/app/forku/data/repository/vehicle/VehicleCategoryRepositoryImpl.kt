package app.forku.data.repository.vehicle

import android.util.Log
import app.forku.data.remote.api.CreateVehicleCategoryRequest
import app.forku.data.remote.api.UpdateVehicleCategoryRequest
import app.forku.data.remote.api.VehicleCategoryApi
import app.forku.data.remote.dto.VehicleCategoryDto
import app.forku.data.remote.dto.toDomain
import app.forku.data.remote.dto.toDto
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleCategoryRepositoryImpl @Inject constructor(
    private val api: VehicleCategoryApi
) : VehicleCategoryRepository {

    override suspend fun getVehicleCategories(): List<VehicleCategory> {
        return try {
            Log.d("VehicleCategoryRepo", "Fetching all vehicle categories")
            val response = api.getVehicleCategories()
            if (!response.isSuccessful) {
                Log.e("VehicleCategoryRepo", "Error fetching categories: ${response.code()}")
                emptyList()
            } else {
                response.body()?.map { it.toDomain() } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("VehicleCategoryRepo", "Error fetching categories", e)
            emptyList()
        }
    }

    override suspend fun getVehicleCategory(id: String): VehicleCategory {
        Log.d("VehicleCategoryRepo", "Fetching vehicle category: $id")
        val response = api.getVehicleCategory(id)
        if (!response.isSuccessful) {
            Log.e("VehicleCategoryRepo", "Error fetching category: ${response.code()}")
            throw Exception("Failed to get vehicle category")
        }
        return response.body()?.toDomain() ?: throw Exception("Category not found")
    }

    override suspend fun createVehicleCategory(name: String, description: String?): VehicleCategory {
        Log.d("VehicleCategoryRepo", "Creating vehicle category: $name")
        try {
            val request = CreateVehicleCategoryRequest(name = name, description = description)
            val response = api.createVehicleCategory(request)
            if (!response.isSuccessful) {
                Log.e("VehicleCategoryRepo", "Error creating category: ${response.code()}")
                throw Exception("Failed to create vehicle category")
            }
            return response.body()?.toDomain() 
                ?: throw Exception("Failed to create vehicle category")
        } catch (e: Exception) {
            Log.e("VehicleCategoryRepo", "Error creating category", e)
            throw e
        }
    }

    override suspend fun updateVehicleCategory(id: String, name: String, description: String?): VehicleCategory {
        Log.d("VehicleCategoryRepo", "Updating vehicle category: $id")
        try {
            val request = UpdateVehicleCategoryRequest(name = name, description = description)
            val response = api.updateVehicleCategory(id, request)
            if (!response.isSuccessful) {
                Log.e("VehicleCategoryRepo", "Error updating category: ${response.code()}")
                throw Exception("Failed to update vehicle category")
            }
            return response.body()?.toDomain() 
                ?: throw Exception("Failed to update vehicle category")
        } catch (e: Exception) {
            Log.e("VehicleCategoryRepo", "Error updating category", e)
            throw e
        }
    }

    override suspend fun deleteVehicleCategory(id: String) {
        Log.d("VehicleCategoryRepo", "Deleting vehicle category: $id")
        try {
            val response = api.deleteVehicleCategory(id)
            if (!response.isSuccessful) {
                Log.e("VehicleCategoryRepo", "Error deleting category: ${response.code()}")
                throw Exception("Failed to delete vehicle category")
            }
        } catch (e: Exception) {
            Log.e("VehicleCategoryRepo", "Error deleting category", e)
            throw e
        }
    }
} 