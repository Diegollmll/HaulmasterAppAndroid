package app.forku.data.repository.checklist

import android.util.Log
import app.forku.core.auth.AuthenticatedRepository
import app.forku.core.business.BusinessContextManager
import app.forku.data.api.ChecklistQuestionVehicleTypeApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.ChecklistQuestionVehicleType
import app.forku.domain.repository.checklist.ChecklistQuestionVehicleTypeRepository
import com.google.gson.Gson
import javax.inject.Inject

class ChecklistQuestionVehicleTypeRepositoryImpl @Inject constructor(
    private val api: ChecklistQuestionVehicleTypeApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager
) : AuthenticatedRepository(), ChecklistQuestionVehicleTypeRepository {

    private val TAG = "ChecklistQuestionVehicleTypeRepo"

    override suspend fun getVehicleTypesByChecklistItemId(checklistItemId: String): List<ChecklistQuestionVehicleType> {
        try {
            Log.d(TAG, "Fetching vehicle types for checklist item: $checklistItemId")
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                val allQuestionVehicleTypes = response.body()!!.map { it.toDomain() }
                val filteredQuestionVehicleTypes = allQuestionVehicleTypes.filter { it.checklistItemId == checklistItemId }
                Log.d(TAG, "Found ${filteredQuestionVehicleTypes.size} vehicle types for checklist item $checklistItemId")
                return filteredQuestionVehicleTypes
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to fetch question vehicle types: ${response.code()} - $errorBody")
                throw Exception("Failed to fetch question vehicle types: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vehicle types for checklist item $checklistItemId", e)
            throw e
        }
    }

    override suspend fun getAllQuestionVehicleTypes(): List<ChecklistQuestionVehicleType> {
        try {
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch all question vehicle types: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all question vehicle types", e)
            throw e
        }
    }

    override suspend fun getQuestionVehicleTypeById(id: String): ChecklistQuestionVehicleType? {
        try {
            val response = api.getById(id)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching question vehicle type by id: $id", e)
            return null
        }
    }

    override suspend fun saveQuestionVehicleTypeAssociation(questionVehicleType: ChecklistQuestionVehicleType): ChecklistQuestionVehicleType {
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId() ?: throw Exception("No business ID available")
            val simplifiedJson = createSimplifiedQuestionVehicleTypeJson(questionVehicleType, businessId)
            
            Log.d(TAG, "Saving question vehicle type association with JSON: $simplifiedJson")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = simplifiedJson,
                businessId = businessId
            )
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Question vehicle type association saved successfully")
                return response.body()!!.toDomain()
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to save question vehicle type association: ${response.code()} - $errorBody")
                throw Exception("Failed to save question vehicle type association: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving question vehicle type association", e)
            throw e
        }
    }

    override suspend fun deleteQuestionVehicleTypeAssociation(id: String): Boolean {
        try {
            val response = api.delete(id)
            return response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting question vehicle type association: $id", e)
            return false
        }
    }

    private fun createSimplifiedQuestionVehicleTypeJson(questionVehicleType: ChecklistQuestionVehicleType, businessId: String): String {
        val isNewQuestionVehicleType = questionVehicleType.id.isBlank()
        
        val jsonObject = mutableMapOf<String, Any?>(
            "\$type" to "ChecklistQuestionVehicleTypeDataObject",
            "ChecklistItemId" to questionVehicleType.checklistItemId,
            "VehicleTypeId" to questionVehicleType.vehicleTypeId,
            "BusinessId" to businessId,
            "IsMarkedForDeletion" to false,
            "InternalObjectId" to 0,
            "IsDirty" to true,
            "IsNew" to isNewQuestionVehicleType
        )
        
        // Handle Id field properly
        if (isNewQuestionVehicleType) {
            jsonObject["Id"] = null
        } else {
            jsonObject["Id"] = questionVehicleType.id
        }
        
        val json = Gson().toJson(jsonObject)
        Log.d(TAG, "Generated JSON: $json")
        return json
    }
} 