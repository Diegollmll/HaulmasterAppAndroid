package app.forku.data.repository.vehicle

import android.util.Log
import app.forku.data.api.VehicleCategoryApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.data.api.dto.vehicle.VehicleCategoryDto
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import app.forku.data.datastore.AuthDataStore
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleCategoryRepositoryImpl @Inject constructor(
    private val api: VehicleCategoryApi,
    private val authDataStore: AuthDataStore
) : VehicleCategoryRepository {

    override suspend fun getVehicleCategories(): List<VehicleCategory> {
        return try {
            Log.d("VehicleCategoryRepo", "Fetching all vehicle categories")
            val response = api.getVehicleCategories()
            if (!response.isSuccessful) {
                Log.e("VehicleCategoryRepo", "Error fetching categories: ${response.code()}")
                emptyList()
            } else {
                response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
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
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("CSRF token not available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("Antiforgery cookie not available")
            
            val category = VehicleCategory(
                id = "",
                name = name,
                description = description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                requiresCertification = false
            )
            
            val dto = category.toDto()
            val entityJson = Gson().toJson(dto)
            
            val response = api.saveVehicleCategory(csrfToken, cookie, entityJson)
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
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("CSRF token not available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("Antiforgery cookie not available")
            
            val category = VehicleCategory(
                id = id,
                name = name,
                description = description,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                requiresCertification = false
            )
            
            val dto = category.toDto().copy(isNew = false) // This is an update
            val entityJson = Gson().toJson(dto)
            
            val response = api.saveVehicleCategory(csrfToken, cookie, entityJson)
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
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("CSRF token not available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("Antiforgery cookie not available")
            
            val response = api.deleteVehicleCategory(id, csrfToken, cookie)
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