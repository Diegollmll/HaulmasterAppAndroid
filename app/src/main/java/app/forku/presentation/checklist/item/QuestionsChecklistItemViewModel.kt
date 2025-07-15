package app.forku.presentation.checklist.item

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.checklist.ChecklistItemRepository
import app.forku.domain.repository.checklist.ChecklistItemCategoryRepository
import app.forku.domain.repository.checklist.ChecklistItemSubcategoryRepository
import app.forku.domain.repository.checklist.ChecklistQuestionVehicleTypeRepository


import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.domain.model.checklist.ChecklistItemCategory
import app.forku.domain.model.checklist.ChecklistItemSubcategory

import app.forku.domain.model.vehicle.EnergySourceEnum
import app.forku.domain.model.checklist.Answer

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionsChecklistItemUiState(
    val checklistId: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedItem: ChecklistItem? = null,
    val isEditMode: Boolean = false,
    val availableEnergySources: List<EnergySourceEnum> = EnergySourceEnum.values().toList(),
    val availableVehicleTypes: List<VehicleType> = emptyList(),
    val availableCategories: List<ChecklistItemCategory> = emptyList(),
    val availableSubcategories: List<ChecklistItemSubcategory> = emptyList(),
    val selectedQuestionVehicleTypeIds: List<String> = emptyList(), // ✅ New: Pre-selected vehicle types for current question
    val userNames: Map<String, String> = emptyMap() // ✅ New: Cache for user names
)

@HiltViewModel
class QuestionsChecklistItemViewModel @Inject constructor(
    private val repository: ChecklistItemRepository,
    private val categoryRepository: ChecklistItemCategoryRepository,
    private val subcategoryRepository: ChecklistItemSubcategoryRepository,
    private val vehicleTypeRepository: VehicleTypeRepository,
    private val questionVehicleTypeRepository: ChecklistQuestionVehicleTypeRepository, // ✅ New: For question-vehicletype associations
    private val userRepository: app.forku.domain.repository.user.UserRepository, // ✅ New: For user tracking
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "QuestionsItemViewModel"
    private val _uiState = MutableStateFlow(QuestionsChecklistItemUiState())
    val uiState: StateFlow<QuestionsChecklistItemUiState> = _uiState.asStateFlow()

    // Remove these separate flows since we're moving to uiState
    // private val _categories = MutableStateFlow<List<ChecklistItemCategory>>(emptyList())
    // val categories: StateFlow<List<ChecklistItemCategory>> = _categories.asStateFlow()

    init {
        // Check if we have a checklistId in the saved state
        savedStateHandle.get<String>("checklistId")?.let { checklistId ->
            setChecklistId(checklistId)
        }
        loadCategories()
        loadVehicleTypes()
        // Energy sources are now static enum values, no need to load from API
    }

    fun setChecklistId(checklistId: String) {
        if (checklistId.isNotBlank() && checklistId != _uiState.value.checklistId) {
            _uiState.update { it.copy(checklistId = checklistId) }
            loadItems()
        }
    }

    fun loadItems() {
        val checklistId = _uiState.value.checklistId
        if (checklistId.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Checklist ID is required to load items"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val items = repository.getChecklistItemsByChecklistId(checklistId)
                Log.d(TAG, "Loaded ${items.size} items for checklist $checklistId")
                
                _uiState.update { 
                    it.copy(
                        items = items,
                        isLoading = false
                    )
                }
                
                // Load user names for items that have goUserId
                loadUserNamesForItems(items)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading items: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error loading items"
                    )
                }
            }
        }
    }

    fun createItem(item: ChecklistItem) {
        val checklistId = _uiState.value.checklistId
        if (checklistId.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Checklist ID is required to create item"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                // Make sure we're using the current checklistId
                val itemWithCorrectId = item.copy(checklistId = checklistId)
                
                Log.d(TAG, "🚀 [CREATE-ITEM] STARTING - Question: '${itemWithCorrectId.question}'")
                Log.d(TAG, "🚀 [CREATE-ITEM] AllVehicleTypesEnabled: ${itemWithCorrectId.allVehicleTypesEnabled}")
                Log.d(TAG, "🚀 [CREATE-ITEM] SupportedVehicleTypeIds: ${itemWithCorrectId.supportedVehicleTypeIds}")
                
                val created = repository.createChecklistItem(itemWithCorrectId)
                Log.d(TAG, "🚀 [CREATE-ITEM] SUCCESS - Created with ID: ${created.id}")
                Log.d(TAG, "🚀 [CREATE-ITEM] Created AllVehicleTypesEnabled: ${created.allVehicleTypesEnabled}")
                
                // Update vehicle type associations for the new item
                Log.d(TAG, "🔍 [ASSOCIATIONS] Item allVehicleTypesEnabled: ${itemWithCorrectId.allVehicleTypesEnabled}")
                Log.d(TAG, "🔍 [ASSOCIATIONS] Item supportedVehicleTypeIds: ${itemWithCorrectId.supportedVehicleTypeIds}")
                
                // If allVehicleTypesEnabled is true, don't create specific associations
                if (!itemWithCorrectId.allVehicleTypesEnabled) {
                    Log.d(TAG, "🔍 [ASSOCIATIONS] Creating specific vehicle type associations")
                    updateQuestionVehicleTypeAssociations(created.id, itemWithCorrectId.supportedVehicleTypeIds)
                } else {
                    // Clear any existing associations since this applies to all vehicle types
                    Log.d(TAG, "🔍 [ASSOCIATIONS] Clearing associations - allVehicleTypesEnabled is true")
                    updateQuestionVehicleTypeAssociations(created.id, emptySet())
                }
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully created question",
                        isLoading = false
                    )
                }
                
                loadItems()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating item: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create item: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun updateItem(item: ChecklistItem) {
        val checklistId = _uiState.value.checklistId
        if (checklistId.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Checklist ID is required to update item"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                if (item.id.isNotBlank()) {
                    Log.d(TAG, "Updating item with id: ${item.id}, question: ${item.question}")
                    
                    // Make sure we're using the current checklistId
                    val itemWithCorrectId = item.copy(checklistId = checklistId)
                    
                    val updated = repository.updateChecklistItem(item.id, itemWithCorrectId)
                    Log.d(TAG, "Item updated successfully: ${updated.id}")
                    
                    // Update vehicle type associations for the updated item
                    Log.d(TAG, "🔍 [UPDATE-ASSOCIATIONS] Item allVehicleTypesEnabled: ${itemWithCorrectId.allVehicleTypesEnabled}")
                    Log.d(TAG, "🔍 [UPDATE-ASSOCIATIONS] Item supportedVehicleTypeIds: ${itemWithCorrectId.supportedVehicleTypeIds}")
                    
                    // If allVehicleTypesEnabled is true, don't create specific associations
                    if (!itemWithCorrectId.allVehicleTypesEnabled) {
                        Log.d(TAG, "🔍 [UPDATE-ASSOCIATIONS] Updating specific vehicle type associations")
                        updateQuestionVehicleTypeAssociations(item.id, itemWithCorrectId.supportedVehicleTypeIds)
                    } else {
                        // Clear any existing associations since this applies to all vehicle types
                        Log.d(TAG, "🔍 [UPDATE-ASSOCIATIONS] Clearing associations - allVehicleTypesEnabled is true")
                        updateQuestionVehicleTypeAssociations(item.id, emptySet())
                    }
                    
                    _uiState.update { 
                        it.copy(
                            successMessage = "Successfully updated question",
                            isLoading = false
                        )
                    }
                    
                    loadItems()
                } else {
                    throw IllegalArgumentException("Cannot update item without an ID")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating item: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update item: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun deleteItem(item: ChecklistItem) {
        if (item.id.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Cannot delete item without ID"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val success = repository.deleteChecklistItem(item.id)
                if (success) {
                    Log.d(TAG, "Item deleted successfully: ${item.id}")
                    
                    _uiState.update { 
                        it.copy(
                            successMessage = "Successfully deleted question",
                            isLoading = false
                        )
                    }
                    
                    loadItems()
                } else {
                    throw Exception("Failed to delete item")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting item: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete item: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun selectItem(item: ChecklistItem) {
        _uiState.update { 
            it.copy(
                selectedItem = item,
                isEditMode = true
            )
        }
        
        // If the item has a category, load its subcategories automatically
        if (item.category.isNotBlank()) {
            Log.d(TAG, "Loading subcategories for selected item's category: ${item.category}")
            loadSubcategoriesForCategory(item.category)
        }
        
        // If the item has an ID, load its vehicle type associations
        if (item.id.isNotBlank()) {
            Log.d(TAG, "Loading vehicle types for selected item: ${item.id}")
            loadVehicleTypesForQuestion(item.id)
        }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                selectedItem = null,
                isEditMode = false
            )
        }
    }

    fun createEmptyItem(): ChecklistItem {
        val checklistId = _uiState.value.checklistId
        
        // Note: goUserId will be set in the repository when creating the item
        return ChecklistItem(
            id = "",
            checklistId = checklistId,
            version = "1.0", // ✅ FIX: Explicit version
            category = "",
            subCategory = "",
            energySourceEnum = listOf(EnergySourceEnum.ELECTRIC),
            vehicleType = emptyList(),
            component = app.forku.domain.model.vehicle.VehicleComponentEnum.FORKS,
            question = "",
            description = "",
            isCritical = false,
            expectedAnswer = Answer.PASS,
            rotationGroup = 1,
            userAnswer = null,
            supportedVehicleTypeIds = emptySet(), // ✅ New field
            goUserId = null, // ✅ Will be set by repository when creating
            allVehicleTypesEnabled = false, // ✅ NEW: Default to false
            createdAt = null, // ✅ FIX: Will be set by backend
            modifiedAt = null // ✅ FIX: Will be set by backend
        )
    }

    fun loadVehicleTypesForQuestion(questionId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading vehicle types for question: $questionId")
                val questionVehicleTypes = questionVehicleTypeRepository.getVehicleTypesByChecklistItemId(questionId)
                val selectedVehicleTypeIds = questionVehicleTypes.map { it.vehicleTypeId }
                
                Log.d(TAG, "Found ${selectedVehicleTypeIds.size} selected vehicle types for question $questionId: $selectedVehicleTypeIds")
                
                _uiState.update { 
                    it.copy(selectedQuestionVehicleTypeIds = selectedVehicleTypeIds)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vehicle types for question $questionId: ${e.message}", e)
                // Don't show error to user, just use empty list as fallback
                _uiState.update { 
                    it.copy(selectedQuestionVehicleTypeIds = emptyList())
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategories()
                if (categories.isEmpty()) {
                    Log.w(TAG, "No categories found from API")
                    _uiState.update { 
                        it.copy(
                            availableCategories = emptyList(),
                            error = "No categories available"
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(availableCategories = categories)
                    }
                    Log.d(TAG, "Loaded ${categories.size} categories from API: ${categories.map { "${it.name} (id: ${it.id})" }}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories from API: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        availableCategories = emptyList(),
                        error = "Failed to load categories: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadSubcategoriesForCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                val subcategories = subcategoryRepository.getSubcategoriesByCategoryId(categoryId)
                _uiState.update { 
                    it.copy(availableSubcategories = subcategories)
                }
                Log.d(TAG, "Loaded ${subcategories.size} subcategories for category $categoryId: ${subcategories.map { "${it.name} (categoryId: ${it.categoryId})" }}")
                
                if (subcategories.isEmpty()) {
                    Log.w(TAG, "No subcategories found for category $categoryId. This might indicate a data mismatch between categories and subcategories in the API.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading subcategories for category $categoryId: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        availableSubcategories = emptyList(),
                        error = "Failed to load subcategories: ${e.message}"
                    )
                }
            }
        }
    }





    private suspend fun updateQuestionVehicleTypeAssociations(questionId: String, newVehicleTypeIds: Set<String>) {
        try {
            Log.d(TAG, "🔗 [ASSOCIATIONS] ========== STARTING ==========")
            Log.d(TAG, "🔗 [ASSOCIATIONS] Question ID: $questionId")
            Log.d(TAG, "🔗 [ASSOCIATIONS] New vehicle type IDs: $newVehicleTypeIds (size: ${newVehicleTypeIds.size})")
            
            // Get current associations from backend
            val currentAssociations = questionVehicleTypeRepository.getVehicleTypesByChecklistItemId(questionId)
            val currentVehicleTypeIds = currentAssociations.map { it.vehicleTypeId }.toSet()
            
            Log.d(TAG, "Current vehicle type IDs: $currentVehicleTypeIds")
            
            // Find vehicle types to add (new selections)
            val vehicleTypesToAdd = newVehicleTypeIds - currentVehicleTypeIds
            Log.d(TAG, "Vehicle types to add: $vehicleTypesToAdd")
            
            // Find vehicle types to remove (deselected)
            val vehicleTypesToRemove = currentVehicleTypeIds - newVehicleTypeIds
            Log.d(TAG, "Vehicle types to remove: $vehicleTypesToRemove")
            
            // Add new vehicle type associations
            vehicleTypesToAdd.forEach { vehicleTypeId ->
                val newAssociation = app.forku.domain.model.checklist.ChecklistQuestionVehicleType(
                    id = "",
                    checklistItemId = questionId,
                    vehicleTypeId = vehicleTypeId,
                    isMarkedForDeletion = false,
                    internalObjectId = 0
                )
                questionVehicleTypeRepository.saveQuestionVehicleTypeAssociation(newAssociation)
                Log.d(TAG, "Added question vehicle type association: $vehicleTypeId")
            }
            
            // Remove deselected vehicle type associations
            val associationsToDelete = currentAssociations.filter { 
                vehicleTypesToRemove.contains(it.vehicleTypeId) 
            }
            
            associationsToDelete.forEach { association ->
                questionVehicleTypeRepository.deleteQuestionVehicleTypeAssociation(association.id)
                Log.d(TAG, "Deleted question vehicle type association: ${association.vehicleTypeId}")
            }
            
            Log.d(TAG, "🔗 [ASSOCIATIONS] ========== COMPLETED SUCCESSFULLY ==========")
            
        } catch (e: Exception) {
            Log.e(TAG, "🔗 [ASSOCIATIONS] ========== ERROR ==========")
            Log.e(TAG, "🔗 [ASSOCIATIONS] Error updating question vehicle type associations: ${e.message}", e)
            // Don't throw here, let the main update continue
        }
    }

    private fun loadVehicleTypes() {
        viewModelScope.launch {
            try {
                val vehicleTypes = vehicleTypeRepository.getVehicleTypes()
                _uiState.update { 
                    it.copy(availableVehicleTypes = vehicleTypes)
                }
                Log.d(TAG, "Loaded ${vehicleTypes.size} vehicle types")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vehicle types: ${e.message}", e)
            }
        }
    }

    /**
     * Load user names for items that have goUserId
     */
    private fun loadUserNamesForItems(items: List<ChecklistItem>) {
        viewModelScope.launch {
            try {
                val userIds = items.mapNotNull { it.goUserId }.distinct()
                if (userIds.isEmpty()) return@launch
                
                Log.d(TAG, "Loading user names for ${userIds.size} users: $userIds")
                
                val newUserNames = mutableMapOf<String, String>()
                userIds.forEach { userId ->
                    try {
                        val user = userRepository.getUserById(userId)
                        val displayName = when {
                            !user?.firstName.isNullOrBlank() && !user?.lastName.isNullOrBlank() -> 
                                "${user?.firstName} ${user?.lastName}"
                            !user?.firstName.isNullOrBlank() -> user?.firstName ?: "Unknown User"
                            !user?.username.isNullOrBlank() -> user?.username ?: "Unknown User"
                            !user?.email.isNullOrBlank() -> user?.email ?: "Unknown User"
                            else -> "Unknown User"
                        }
                        newUserNames[userId] = displayName
                        Log.d(TAG, "Loaded user name: $userId -> $displayName")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load user $userId: ${e.message}")
                        newUserNames[userId] = "Unknown User"
                    }
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        userNames = currentState.userNames + newUserNames
                    )
                }
                
                Log.d(TAG, "User names cache updated with ${newUserNames.size} entries")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user names for items", e)
            }
        }
    }
    
    /**
     * Get user display name by user ID with fallbacks
     */
    fun getUserName(userId: String?): String {
        return when {
            userId.isNullOrBlank() -> "System"
            _uiState.value.userNames.containsKey(userId) -> _uiState.value.userNames[userId]!!
            else -> "Unknown User"
        }
    }


} 