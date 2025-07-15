package app.forku.data.repository.checklist

import app.forku.data.api.ChecklistItemApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.repository.checklist.ChecklistItemRepository
import app.forku.data.datastore.AuthDataStore
import app.forku.core.business.BusinessContextManager
import android.util.Log
import com.google.gson.Gson
import javax.inject.Inject

class ChecklistItemRepositoryImpl @Inject constructor(
    private val api: ChecklistItemApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager,
    private val userRepository: app.forku.domain.repository.user.UserRepository
) : ChecklistItemRepository {

    private val TAG = "ChecklistItemRepo"

    override suspend fun getAllChecklistItems(): List<ChecklistItem> {
        try {
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Fetched ${response.body()!!.size} checklist items")
                return response.body()!!.map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch checklist items: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all checklist items", e)
            throw e
        }
    }

    override suspend fun getChecklistItemsByChecklistId(checklistId: String): List<ChecklistItem> {
        try {
            val allItems = getAllChecklistItems()
            val filteredItems = allItems.filter { it.checklistId == checklistId }
            Log.d(TAG, "Found ${filteredItems.size} items for checklist $checklistId")
            return filteredItems
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching items for checklist $checklistId", e)
            throw e
        }
    }

    override suspend fun getChecklistItemById(id: String): ChecklistItem? {
        try {
            val response = api.getById(id)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching checklist item by id: $id", e)
            return null
        }
    }

    override suspend fun createChecklistItem(item: ChecklistItem): ChecklistItem {
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId() ?: throw Exception("No business ID available")
            
            // Get current user ID to assign as creator
            val currentUserId = userRepository.getCurrentUserId()
            Log.d(TAG, "🔍 [DEBUG-ITEM] Getting current user ID for checklist item...")
            Log.d(TAG, "🔍 [DEBUG-ITEM] Current user ID result: '$currentUserId'")
            
            // Create item with current user as creator
            val itemWithCreator = item.copy(goUserId = currentUserId)
            Log.d(TAG, "🔍 [DEBUG-ITEM] Original item goUserId: '${item.goUserId}'")
            Log.d(TAG, "🔍 [DEBUG-ITEM] Updated item goUserId: '${itemWithCreator.goUserId}'")
            
            // Create simplified JSON directly from domain model (same pattern as ChecklistRepository)
            val simplifiedJson = createSimplifiedChecklistItemJson(itemWithCreator, businessId)
            
            Log.d(TAG, "Creating checklist item with simplified JSON: $simplifiedJson")
            Log.d(TAG, "CSRF Token: $csrfToken")
            Log.d(TAG, "Cookie: $cookie")
            Log.d(TAG, "BusinessId: $businessId")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = simplifiedJson,
                businessId = businessId
            )
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Created checklist item with id: ${response.body()!!.Id}")
                return response.body()!!.toDomain()
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to create checklist item: ${response.code()}")
                Log.e(TAG, "Response body: $errorBody")
                Log.e(TAG, "Request JSON was: $simplifiedJson")
                throw Exception("Failed to create checklist item: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating checklist item", e)
            throw e
        }
    }

    override suspend fun updateChecklistItem(id: String, item: ChecklistItem): ChecklistItem {
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val businessId = businessContextManager.getCurrentBusinessId() ?: throw Exception("No business ID available")
            
            // For updates, preserve existing goUserId if not provided
            val itemWithId = if (item.goUserId.isNullOrEmpty()) {
                // Try to get existing item to preserve goUserId
                val existingItem = getChecklistItemById(id)
                item.copy(id = id, goUserId = existingItem?.goUserId)
            } else {
                item.copy(id = id)
            }
            
            Log.d(TAG, "🔍 [DEBUG-ITEM-UPDATE] Updating item with goUserId: '${itemWithId.goUserId}'")
            
            val simplifiedJson = createSimplifiedChecklistItemJson(itemWithId, businessId)
            
            Log.d(TAG, "Updating checklist item with simplified JSON: $simplifiedJson")
            Log.d(TAG, "CSRF Token: $csrfToken")
            Log.d(TAG, "Cookie: $cookie")
            Log.d(TAG, "BusinessId: $businessId")
            
            val response = api.save(
                csrfToken = csrfToken,
                cookie = cookie,
                entity = simplifiedJson,
                businessId = businessId
            )
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Updated checklist item with id: $id")
                return response.body()!!.toDomain()
            } else {
                Log.e(TAG, "Failed to update checklist item: ${response.code()}")
                Log.e(TAG, "Response body: ${response.errorBody()?.string()}")
                throw Exception("Failed to update checklist item: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating checklist item: $id", e)
            throw e
        }
    }

    override suspend fun deleteChecklistItem(id: String): Boolean {
        try {
            val response = api.delete(id)
            val success = response.isSuccessful
            Log.d(TAG, "Delete checklist item $id: $success")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting checklist item: $id", e)
            return false
        }
    }

    /**
     * Creates a simplified JSON string directly from ChecklistItem domain model
     * This matches the exact format expected by the GO Platform backend
     * Similar to the pattern used in ChecklistRepositoryImpl
     */
    private fun createSimplifiedChecklistItemJson(item: ChecklistItem, businessId: String): String {
        // Log individual fields for debugging
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] ========== CREATING JSON ==========")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] QUESTION: '${item.question}'")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] 🎯 AllVehicleTypesEnabled: ${item.allVehicleTypesEnabled}")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - ChecklistId: ${item.checklistId}")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - Category: ${item.category}")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - SubCategory: ${item.subCategory}")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - EnergySource: ${item.energySourceEnum.map { it.ordinal }}")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - VehicleComponent: ${item.component.value}")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - Id: '${item.id}' (empty: ${item.id.isBlank()})")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - BusinessId: $businessId")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - GOUserId: '${item.goUserId}'")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] - SupportedVehicleTypeIds: ${item.supportedVehicleTypeIds}")
        
        // Determine if this is a new item
        val isNewItem = item.id.isBlank()
        
        // Build the JSON object, always include Id field
        val jsonObject = mutableMapOf<String, Any?>(
            "\$type" to "ChecklistItemDataObject",
            "ChecklistId" to item.checklistId,
            "ChecklistItemCategoryId" to item.category,
            "ChecklistItemSubcategoryId" to item.subCategory,
            "Description" to (item.description.takeIf { it.isNotBlank() } ?: ""),
            "EnergySource" to item.energySourceEnum.map { it.ordinal },
            "ExpectedAnswer" to item.expectedAnswer.ordinal,
            "IsCritical" to item.isCritical,
            "Question" to item.question,
            "RotationGroup" to item.rotationGroup,
            "VehicleComponent" to item.component.value,
            "BusinessId" to businessId, // ✅ CRITICAL: Include BusinessId for multitenancy
            "GOUserId" to item.goUserId, // ✅ New: Include creator user ID
            "AllVehicleTypesEnabled" to item.allVehicleTypesEnabled, // ✅ NEW: Include AllVehicleTypesEnabled field
            "IsMarkedForDeletion" to false,
            "InternalObjectId" to 0,
            "IsDirty" to true, // Always true for new/modified items
            "IsNew" to isNewItem // True if ID is empty (new item)
        )
        
        // Include Id field: null for new items, actual id for updates
        if (isNewItem) {
            jsonObject["Id"] = null
        } else {
            jsonObject["Id"] = item.id
        }
        
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] Map GOUserId value: '${jsonObject["GOUserId"]}'")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] Map BusinessId value: '${jsonObject["BusinessId"]}'")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] 🎯 Map AllVehicleTypesEnabled value: '${jsonObject["AllVehicleTypesEnabled"]}'")
        
        val jsonString = Gson().toJson(jsonObject)
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] ========== FINAL JSON ==========")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] $jsonString")
        Log.d(TAG, "📝 [JSON-DEBUG-ITEM] ========== END JSON ==========")
        
        return jsonString
    }
} 