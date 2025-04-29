package app.forku.data.repository

import android.util.Log
import app.forku.data.api.VehicleComponentApi
import app.forku.data.api.dto.vehicle.VehicleComponentDto
import app.forku.domain.repository.vehicle.VehicleComponentRepository
import retrofit2.Response
import javax.inject.Inject

class VehicleComponentRepositoryImpl @Inject constructor(
    private val api: VehicleComponentApi
) : VehicleComponentRepository {
    
    private val TAG = "VehicleComponentRepo"

    override suspend fun getAllComponents(): Response<List<VehicleComponentDto>> {
        return try {
            val response = api.getAllComponents()
            Log.d(TAG, "GET all components - ${response.raw().request.url}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error getting components: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getComponentById(id: String): Response<VehicleComponentDto> {
        return try {
            val response = api.getComponentById(id)
            Log.d(TAG, "GET component by id: $id - ${response.raw().request.url}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error getting component by id: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createComponent(component: VehicleComponentDto): Response<VehicleComponentDto> {
        return try {
            val response = api.saveComponent(component)
            Log.d(TAG, "POST save component (create) - ${response.raw().request.url}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error creating component: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateComponent(id: String, component: VehicleComponentDto): Response<VehicleComponentDto> {
        return try {
            val response = api.saveComponent(component.copy(id = id))
            Log.d(TAG, "POST save component (update) - ${response.raw().request.url}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error updating component: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteComponent(id: String): Response<Unit> {
        return try {
            val response = api.deleteComponent(id)
            Log.d(TAG, "DELETE component - ${response.raw().request.url}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting component: ${e.message}", e)
            throw e
        }
    }
} 