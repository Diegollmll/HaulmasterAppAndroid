package app.forku.data.repository.checklist

import android.util.Log
import app.forku.core.auth.AuthenticatedRepository
import app.forku.core.business.BusinessContextManager
import app.forku.data.api.ChecklistVehicleTypeApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.ChecklistVehicleType
import app.forku.domain.repository.checklist.ChecklistVehicleTypeRepository
import com.google.gson.Gson
import javax.inject.Inject

class ChecklistVehicleTypeRepositoryImpl @Inject constructor(
    private val api: ChecklistVehicleTypeApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager
) : AuthenticatedRepository(), ChecklistVehicleTypeRepository {

    private val TAG = "ChecklistVehicleTypeRepo"

    override suspend fun getVehicleTypesByChecklistId(checklistId: String): List<ChecklistVehicleType> {
        try {
            Log.d(TAG, "Fetching vehicle types for checklist: $checklistId")
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                val allVehicleTypes = response.body()!!.map { it.toDomain() }
                val filteredVehicleTypes = allVehicleTypes.filter { it.checklistId == checklistId }
                Log.d(TAG, "Found ${filteredVehicleTypes.size} vehicle types for checklist $checklistId")
                return filteredVehicleTypes
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to fetch vehicle types: ${response.code()} - $errorBody")
                throw Exception("Failed to fetch vehicle types: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle types for checklist $checklistId", e)
            throw e
        }
    }

    override suspend fun getAllVehicleTypes(): List<ChecklistVehicleType> {
        try {
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch all vehicle types: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all vehicle types", e)
            throw e
        }
    }

    override suspend fun getVehicleTypeById(id: String): ChecklistVehicleType? {
        try {
            val response = api.getById(id)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle type by id: $id", e)
            return null
        }
    }

    override suspend fun saveVehicleTypeAssociation(vehicleType: ChecklistVehicleType): ChecklistVehicleType {
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId() ?: throw Exception("No business ID available")
            val simplifiedJson = createSimplifiedVehicleTypeJson(vehicleType, businessId)
            
            Log.d(TAG, "Saving vehicle type association with JSON: $simplifiedJson")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = simplifiedJson,
                businessId = businessId
            )
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Vehicle type association saved successfully")
                return response.body()!!.toDomain()
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to save vehicle type association: ${response.code()} - $errorBody")
                throw Exception("Failed to save vehicle type association: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving vehicle type association", e)
            throw e
        }
    }

    override suspend fun deleteVehicleTypeAssociation(id: String): Boolean {
        try {
            val response = api.delete(id)
            return response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting vehicle type association: $id", e)
            return false
        }
    }

    private fun createSimplifiedVehicleTypeJson(vehicleType: ChecklistVehicleType, businessId: String): String {
        val isNewVehicleType = vehicleType.id.isBlank()
        
        val jsonObject = mutableMapOf<String, Any?>(
            "\$type" to "ChecklistVehicleTypeDataObject",
            "ChecklistId" to vehicleType.checklistId,
            "VehicleTypeId" to vehicleType.vehicleTypeId,
            "BusinessId" to businessId,
            "IsMarkedForDeletion" to false,
            "InternalObjectId" to 0,
            "IsDirty" to true,
            "IsNew" to isNewVehicleType
        )
        
        // Handle Id field properly
        if (isNewVehicleType) {
            jsonObject["Id"] = null
        } else {
            jsonObject["Id"] = vehicleType.id
        }
        
        val json = Gson().toJson(jsonObject)
        Log.d(TAG, "Generated JSON: $json")
        return json
    }
} 