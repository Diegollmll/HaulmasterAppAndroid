package app.forku.data.repository.checklist

import android.util.Log
import app.forku.core.auth.AuthenticatedRepository
import app.forku.core.business.BusinessContextManager
import app.forku.data.api.ChecklistChecklistItemCategoryApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.ChecklistChecklistItemCategory
import app.forku.domain.repository.checklist.ChecklistChecklistItemCategoryRepository
import com.google.gson.Gson
import javax.inject.Inject

class ChecklistChecklistItemCategoryRepositoryImpl @Inject constructor(
    private val api: ChecklistChecklistItemCategoryApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager
) : AuthenticatedRepository(), ChecklistChecklistItemCategoryRepository {

    private val TAG = "ChecklistChecklistItemCategoryRepo"

    override suspend fun getCategoriesByChecklistId(checklistId: String): List<ChecklistChecklistItemCategory> {
        try {
            Log.d(TAG, "Fetching categories for checklist: $checklistId")
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                val allCategories = response.body()!!.map { it.toDomain() }
                val filteredCategories = allCategories.filter { it.checklistId == checklistId }
                Log.d(TAG, "Found ${filteredCategories.size} categories for checklist $checklistId")
                return filteredCategories
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to fetch categories: ${response.code()} - $errorBody")
                throw Exception("Failed to fetch categories: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories for checklist $checklistId", e)
            throw e
        }
    }

    override suspend fun getAllCategories(): List<ChecklistChecklistItemCategory> {
        try {
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch all categories: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all categories", e)
            throw e
        }
    }

    override suspend fun getCategoryById(id: String): ChecklistChecklistItemCategory? {
        try {
            val response = api.getById(id)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching category by id: $id", e)
            return null
        }
    }

    override suspend fun saveCategoryAssociation(category: ChecklistChecklistItemCategory): ChecklistChecklistItemCategory {
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId() ?: throw Exception("No business ID available")
            val simplifiedJson = createSimplifiedCategoryJson(category, businessId)
            
            Log.d(TAG, "Saving category association with JSON: $simplifiedJson")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = simplifiedJson,
                businessId = businessId
            )
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Category association saved successfully")
                return response.body()!!.toDomain()
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to save category association: ${response.code()} - $errorBody")
                throw Exception("Failed to save category association: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving category association", e)
            throw e
        }
    }

    override suspend fun deleteCategoryAssociation(id: String): Boolean {
        try {
            val response = api.delete(id)
            return response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category association: $id", e)
            return false
        }
    }

    private fun createSimplifiedCategoryJson(category: ChecklistChecklistItemCategory, businessId: String): String {
        val isNewCategory = category.id.isBlank()
        
        val jsonObject = mutableMapOf<String, Any?>(
            "\$type" to "ChecklistChecklistItemCategoryDataObject",
            "ChecklistId" to category.checklistId,
            "ChecklistItemCategoryId" to category.checklistItemCategoryId,
            "BusinessId" to businessId,
            "IsMarkedForDeletion" to false,
            "InternalObjectId" to 0,
            "IsDirty" to true,
            "IsNew" to isNewCategory
        )
        
        // Handle Id field properly
        if (isNewCategory) {
            jsonObject["Id"] = null
        } else {
            jsonObject["Id"] = category.id
        }
        
        val json = Gson().toJson(jsonObject)
        Log.d(TAG, "Generated JSON: $json")
        return json
    }
} 