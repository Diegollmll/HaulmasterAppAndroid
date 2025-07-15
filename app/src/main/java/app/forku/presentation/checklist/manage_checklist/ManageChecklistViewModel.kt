package app.forku.presentation.checklist.manage_checklist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistChecklistItemCategoryRepository
import app.forku.domain.repository.checklist.ChecklistVehicleTypeRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.core.business.BusinessContextManager
import app.forku.presentation.common.components.BusinessContextUpdater
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageChecklistUiState(
    val checklists: List<Checklist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedChecklist: Checklist? = null,
    val isEditMode: Boolean = false,
    val availableVehicleTypes: List<app.forku.domain.model.vehicle.VehicleType> = emptyList(),
    val availableCategories: List<app.forku.domain.model.checklist.ChecklistItemCategory> = emptyList(),
    val selectedCategoryIds: List<String> = emptyList(), // ✅ New: Pre-selected categories for current checklist
    val availableEnergySources: List<app.forku.domain.model.vehicle.EnergySourceEnum> = app.forku.domain.model.vehicle.EnergySourceEnum.values().toList(),
    val availableCriticalityLevels: List<Int> = listOf(0, 1),
    val defaultValues: app.forku.data.api.ChecklistDefaultsDto? = null,
    val userNames: Map<String, String> = emptyMap() // ✅ New: Cache for user names by userId
)

@HiltViewModel
class ManageChecklistViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    override val businessContextManager: BusinessContextManager,
    private val vehicleTypeRepository: app.forku.domain.repository.vehicle.VehicleTypeRepository,
    private val categoryRepository: app.forku.domain.repository.checklist.ChecklistItemCategoryRepository,
    private val checklistCategoryRepository: ChecklistChecklistItemCategoryRepository, // ✅ For checklist-category associations
    private val checklistVehicleTypeRepository: ChecklistVehicleTypeRepository, // ✅ New: For checklist-vehicletype associations
    private val userRepository: UserRepository, // ✅ New: For getting user information
    private val checklistApi: app.forku.data.api.ChecklistApi,
    override val userPreferencesRepository: app.forku.domain.repository.user.UserPreferencesRepository
) : ViewModel(), BusinessContextUpdater {

    private val TAG = "ManageChecklistViewModel"
    private val _uiState = MutableStateFlow(ManageChecklistUiState())
    val uiState: StateFlow<ManageChecklistUiState> = _uiState.asStateFlow()

    init {
        loadChecklists()
        loadVehicleTypes()
        loadCategories()
        loadDefaultValues()
        logAvailableOptions()
    }

    private fun logAvailableOptions() {
        Log.d(TAG, "Available Energy Sources:")
        app.forku.domain.model.vehicle.EnergySourceEnum.values().forEach { source ->
            Log.d(TAG, "- ${source.name} (apiValue: ${source.apiValue})")
        }
    }

    fun loadChecklists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val businessId = businessContextManager.getCurrentBusinessId()
                Log.d(TAG, "Loading checklists for management for businessId: $businessId")
                
                val checklists = checklistRepository.getChecklistsForManagement(businessId)
                Log.d(TAG, "Loaded ${checklists.size} checklists for management")
                
                // Load user names for checklists that have goUserId
                loadUserNamesForChecklists(checklists)
                
                _uiState.update { 
                    it.copy(
                        checklists = checklists,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading checklists: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error loading checklists"
                    ) 
                }
            }
        }
    }

    fun loadVehicleTypes() {
        viewModelScope.launch {
            try {
                val types = vehicleTypeRepository.getVehicleTypes()
                _uiState.update { it.copy(availableVehicleTypes = types) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vehicle types: ${e.message}", e)
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategories()
                _uiState.update { it.copy(availableCategories = categories) }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories: ${e.message}", e)
            }
        }
    }

    fun loadSelectedCategoriesForChecklist(checklistId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading selected categories for checklist: $checklistId")
                val checklistCategories = checklistCategoryRepository.getCategoriesByChecklistId(checklistId)
                val selectedCategoryIds = checklistCategories.map { it.checklistItemCategoryId }
                
                Log.d(TAG, "Found ${selectedCategoryIds.size} selected categories for checklist $checklistId: $selectedCategoryIds")
                
                _uiState.update { 
                    it.copy(selectedCategoryIds = selectedCategoryIds)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading selected categories for checklist $checklistId: ${e.message}", e)
                // Don't show error to user, just use empty list as fallback
                _uiState.update { 
                    it.copy(selectedCategoryIds = emptyList())
                }
            }
        }
    }

    fun loadDefaultValues() {
        viewModelScope.launch {
            try {
                // TODO: The /api/checklist/metadata endpoint doesn't exist
                // For now, use fallback defaults until we implement proper metadata loading
                Log.d(TAG, "Using fallback default values (metadata endpoint not available)")
                _uiState.update { 
                    it.copy(
                        defaultValues = app.forku.data.api.ChecklistDefaultsDto(),
                        availableCriticalityLevels = listOf(0, 1)
                    ) 
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading default values: ${e.message}", e)
                // Use fallback defaults if API fails
                _uiState.update { 
                    it.copy(
                        defaultValues = app.forku.data.api.ChecklistDefaultsDto(),
                        availableCriticalityLevels = listOf(0, 1)
                    ) 
                }
            }
        }
    }

    fun createChecklist(checklist: Checklist) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                Log.d(TAG, "Creating checklist: ${checklist.title}")
                
                // Get current user ID to assign as creator
                val currentUserId = userRepository.getCurrentUserId()
                Log.d(TAG, "🔍 [DEBUG] Getting current user ID...")
                Log.d(TAG, "🔍 [DEBUG] Current user ID result: '$currentUserId'")
                Log.d(TAG, "🔍 [DEBUG] Is current user ID null? ${currentUserId == null}")
                Log.d(TAG, "🔍 [DEBUG] Is current user ID empty? ${currentUserId?.isEmpty()}")
                
                // Also try to get current user for additional debug info
                val currentUser = userRepository.getCurrentUser()
                Log.d(TAG, "🔍 [DEBUG] Current user: ${currentUser?.username} (id: ${currentUser?.id})")
                
                // Create checklist with current user as creator
                val checklistWithCreator = checklist.copy(goUserId = currentUserId)
                Log.d(TAG, "🔍 [DEBUG] Original checklist goUserId: '${checklist.goUserId}'")
                Log.d(TAG, "🔍 [DEBUG] Updated checklist goUserId: '${checklistWithCreator.goUserId}'")
                val created = checklistRepository.createChecklist(checklistWithCreator)
                Log.d(TAG, "Checklist created successfully with ID: ${created.id}")
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully created '${checklist.title}'",
                        isLoading = false
                    )
                }
                
                loadChecklists()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating checklist: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create checklist: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun updateChecklist(checklist: Checklist) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                Log.d(TAG, "Updating checklist with id: ${checklist.id}")
                
                // Update category associations separately to handle deletions properly
                updateCategoryAssociations(checklist.id, checklist.requiredCategoryIds)
                
                // Update vehicle type associations separately to handle deletions properly
                updateVehicleTypeAssociations(checklist.id, checklist.supportedVehicleTypeIds)
                
                val updated = checklistRepository.updateChecklist(checklist.id, checklist)
                Log.d(TAG, "Checklist updated successfully: ${updated.id}")
                
                _uiState.update { 
                    it.copy(
                        successMessage = "Successfully updated '${checklist.title}'",
                        isLoading = false
                    )
                }
                
                loadChecklists()
            } catch (e: Exception) {
                Log.e(TAG, "Error updating checklist: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update checklist: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun deleteChecklist(checklist: Checklist) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                Log.d(TAG, "Deleting checklist with id: ${checklist.id}")
                val deleted = checklistRepository.deleteChecklist(checklist.id)
                
                if (deleted) {
                    Log.d(TAG, "Checklist deleted successfully")
                    _uiState.update { 
                        it.copy(
                            successMessage = "Successfully deleted '${checklist.title}'",
                            isLoading = false
                        )
                    }
                    loadChecklists()
                } else {
                    throw Exception("Failed to delete checklist")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting checklist: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete checklist: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun selectChecklist(checklist: Checklist?) {
        _uiState.update { 
            it.copy(
                selectedChecklist = checklist,
                isEditMode = checklist != null
            )
        }
        
        // Load selected categories when a checklist is selected for editing
        if (checklist != null) {
            loadSelectedCategoriesForChecklist(checklist.id)
        } else {
            // Clear selected categories when no checklist is selected
            _uiState.update { it.copy(selectedCategoryIds = emptyList()) }
        }
    }

    fun clearSelection() {
        _uiState.update { 
            it.copy(
                selectedChecklist = null,
                isEditMode = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    suspend fun createDefaultChecklistWithBusinessId(title: String, description: String = ""): Checklist {
        // Business ID is handled automatically by the repository layer
        return createDefaultChecklist(title, description)
    }

    fun selectNewChecklist(title: String, description: String = "") {
        viewModelScope.launch {
            val newChecklist = createDefaultChecklistWithBusinessId(title, description)
            selectChecklist(newChecklist)
        }
    }

    fun createNewTestChecklist(title: String, description: String = "") {
        viewModelScope.launch {
            val testChecklist = createDefaultChecklistWithBusinessId(title, description)
            createChecklist(testChecklist)
        }
    }

    fun createDefaultChecklist(title: String, description: String = "", businessId: String? = null): Checklist {
        // Use the current business ID from the context state if available
        val effectiveBusinessId = businessId ?: businessContextManager.contextState.value.businessId
        
        // Get default values from state or use fallbacks
        val defaults = _uiState.value.defaultValues ?: app.forku.data.api.ChecklistDefaultsDto()
        
        return Checklist(
            id = "",
            title = title,
            description = description,
            businessId = effectiveBusinessId,
            items = emptyList(),
            criticalityLevels = defaults.defaultCriticalityLevels,
            criticalQuestionMinimum = defaults.defaultCriticalQuestionMinimum,
            energySources = defaults.defaultEnergySources,
            isDefault = false,
            maxQuestionsPerCheck = defaults.defaultMaxQuestionsPerCheck,
            rotationGroups = defaults.defaultRotationGroups,
            standardQuestionMaximum = defaults.defaultStandardQuestionMaximum,
            isMarkedForDeletion = false,
            internalObjectId = 0,
            allVehicleTypesEnabled = true,
            supportedVehicleTypeIds = emptySet(),
            requiredCategoryIds = emptySet()
        )
    }

    fun isChecklistEditable(checklist: Checklist): Boolean {
        // Default checklists (without businessId) are read-only
        return checklist.businessId != null
    }

    private suspend fun updateCategoryAssociations(checklistId: String, newCategoryIds: Set<String>) {
        try {
            Log.d(TAG, "Updating category associations for checklist $checklistId")
            Log.d(TAG, "New category IDs: $newCategoryIds")
            
            // Get current associations from backend
            val currentAssociations = checklistCategoryRepository.getCategoriesByChecklistId(checklistId)
            val currentCategoryIds = currentAssociations.map { it.checklistItemCategoryId }.toSet()
            
            Log.d(TAG, "Current category IDs: $currentCategoryIds")
            
            // Find categories to add (new selections)
            val categoriesToAdd = newCategoryIds - currentCategoryIds
            Log.d(TAG, "Categories to add: $categoriesToAdd")
            
            // Find categories to remove (deselected)
            val categoriesToRemove = currentCategoryIds - newCategoryIds
            Log.d(TAG, "Categories to remove: $categoriesToRemove")
            
            // Add new category associations
            categoriesToAdd.forEach { categoryId ->
                val newAssociation = app.forku.domain.model.checklist.ChecklistChecklistItemCategory(
                    id = "",
                    checklistId = checklistId,
                    checklistItemCategoryId = categoryId,
                    isMarkedForDeletion = false,
                    internalObjectId = 0
                )
                checklistCategoryRepository.saveCategoryAssociation(newAssociation)
                Log.d(TAG, "Added category association: $categoryId")
            }
            
            // Remove deselected category associations
            val associationsToDelete = currentAssociations.filter { 
                categoriesToRemove.contains(it.checklistItemCategoryId) 
            }
            
            associationsToDelete.forEach { association ->
                checklistCategoryRepository.deleteCategoryAssociation(association.id)
                Log.d(TAG, "Deleted category association: ${association.checklistItemCategoryId}")
            }
            
            Log.d(TAG, "Category associations updated successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category associations: ${e.message}", e)
            // Don't throw here, let the main update continue
        }
    }

    private suspend fun updateVehicleTypeAssociations(checklistId: String, newVehicleTypeIds: Set<String>) {
        try {
            Log.d(TAG, "Updating vehicle type associations for checklist $checklistId")
            Log.d(TAG, "New vehicle type IDs: $newVehicleTypeIds")
            
            // Get current associations from backend
            val currentAssociations = checklistVehicleTypeRepository.getVehicleTypesByChecklistId(checklistId)
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
                val newAssociation = app.forku.domain.model.checklist.ChecklistVehicleType(
                    id = "",
                    checklistId = checklistId,
                    vehicleTypeId = vehicleTypeId,
                    isMarkedForDeletion = false,
                    internalObjectId = 0
                )
                checklistVehicleTypeRepository.saveVehicleTypeAssociation(newAssociation)
                Log.d(TAG, "Added vehicle type association: $vehicleTypeId")
            }
            
            // Remove deselected vehicle type associations
            val associationsToDelete = currentAssociations.filter { 
                vehicleTypesToRemove.contains(it.vehicleTypeId) 
            }
            
            associationsToDelete.forEach { association ->
                checklistVehicleTypeRepository.deleteVehicleTypeAssociation(association.id)
                Log.d(TAG, "Deleted vehicle type association: ${association.vehicleTypeId}")
            }
            
            Log.d(TAG, "Vehicle type associations updated successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vehicle type associations: ${e.message}", e)
            // Don't throw here, let the main update continue
        }
    }
    
    private fun loadUserNamesForChecklists(checklists: List<Checklist>) {
        viewModelScope.launch {
            try {
                val userIds = checklists.mapNotNull { it.goUserId }.distinct()
                if (userIds.isNotEmpty()) {
                    Log.d(TAG, "Loading user names for ${userIds.size} unique users")
                    
                    val userNames = mutableMapOf<String, String>()
                    userIds.forEach { userId ->
                        try {
                            val user = userRepository.getUserById(userId)
                            if (user != null) {
                                userNames[userId] = user.fullName
                                Log.d(TAG, "Loaded user name: $userId -> ${user.fullName}")
                            } else {
                                userNames[userId] = "Unknown User"
                                Log.w(TAG, "User not found for ID: $userId")
                            }
                        } catch (e: Exception) {
                            userNames[userId] = "Unknown User"
                            Log.e(TAG, "Error loading user $userId: ${e.message}", e)
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(userNames = userNames)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user names: ${e.message}", e)
                // Don't update UI state on error, just continue with empty user names
            }
        }
    }

    fun getUserName(userId: String?): String {
        return if (userId != null) {
            _uiState.value.userNames[userId] ?: "Unknown User"
        } else {
            "System"
        }
    }
    
    /**
     * Implementation of BusinessContextUpdater interface
     * Reloads checklists when context changes
     */
    override fun reloadData() {
        loadChecklists()
    }
} 