package app.forku.presentation.vehicle.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.EnergySourceEnum
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class EditVehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val vehicleTypeRepository: VehicleTypeRepository,
    private val businessRepository: BusinessRepository,
    private val vehicleCategoryRepository: VehicleCategoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(EditVehicleState())
    val state: StateFlow<EditVehicleState> = _state

    private val vehicleId: String? = savedStateHandle.get<String>("vehicleId")
    // Assuming businessId might also be passed for non-admin users, or we derive it
    private val businessId: String? = savedStateHandle.get<String>("businessId") 

    init {
        Log.d("EditVehicleViewModel", "Initializing for vehicleId: $vehicleId, businessId: $businessId")
        if (vehicleId != null) {
            _state.update { it.copy(vehicleId = vehicleId) }
            loadInitialData()
        } else {
            _state.update { it.copy(error = "Vehicle ID not provided.") }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // 1. Get Current User Role
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update { it.copy(isLoading = false, error = "User not authenticated") }
                    return@launch
                }
                _state.update { it.copy(currentUserRole = currentUser.role) }

                // 2. Load necessary dropdown data (Categories, Businesses if admin)
                loadVehicleCategories()
                if (currentUser.role == UserRole.SYSTEM_OWNER || currentUser.role == UserRole.SUPERADMIN) {
                    loadBusinesses()
                }

                // 3. Load Vehicle Data
                val effectiveBusinessId = when (currentUser.role) {
                    UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> businessId ?: "0" // Use passed ID or placeholder
                    else -> currentUser.businessId ?: run {
                        _state.update { it.copy(isLoading = false, error = "Business context missing") }
                        return@launch
                    }
                }
                
                Log.d("EditVehicleViewModel", "Loading vehicle $vehicleId with businessId $effectiveBusinessId")
                val vehicle = vehicleRepository.getVehicle(vehicleId!!, effectiveBusinessId)
                Log.d("EditVehicleViewModel", "Loaded vehicle: ${vehicle.codename}, businessId: ${vehicle.businessId}")

                // 4. Load Vehicle Types based on the vehicle's category
                loadVehicleTypes(vehicle.type.VehicleCategoryId)

                // 5. Update State with loaded data
                _state.update { currentState ->
                    currentState.copy(
                        initialVehicle = vehicle,
                        selectedCategory = currentState.vehicleCategories.find { it.id == vehicle.type.VehicleCategoryId },
                        selectedType = vehicle.type,
                        selectedEnergySourceEnum = EnergySourceEnum.valueOf(vehicle.energyType.uppercase()),
                        selectedBusinessId = vehicle.businessId, // Store the actual businessId from the vehicle
                        loadSuccess = true,
                        isLoading = false
                    )
                }
                Log.d("EditVehicleViewModel", "State updated with vehicle data. Selected businessId: ${_state.value.selectedBusinessId}")

            } catch (e: Exception) {
                Log.e("EditVehicleViewModel", "Error loading initial data for vehicle $vehicleId", e)
                _state.update { it.copy(isLoading = false, error = "Failed to load vehicle data: ${e.message}") }
            }
        }
    }

    private suspend fun loadVehicleCategories() {
        try {
            val categories = vehicleCategoryRepository.getVehicleCategories()
            _state.update { it.copy(vehicleCategories = categories) }
        } catch (e: Exception) {
            Log.w("EditVehicleViewModel", "Failed to load vehicle categories", e)
            // Non-critical, maybe show error later
        }
    }

    private suspend fun loadBusinesses() {
        try {
            val businesses = businessRepository.getAllBusinesses()
            _state.update { it.copy(businesses = businesses) }
        } catch (e: Exception) {
            Log.w("EditVehicleViewModel", "Failed to load businesses", e)
             // Non-critical
        }
    }

    private suspend fun loadVehicleTypes(categoryId: String) {
        try {
            Log.d("EditVehicleViewModel", "Loading types for category: $categoryId")
            val types = vehicleTypeRepository.getVehicleTypesByCategory(categoryId)
            Log.d("EditVehicleViewModel", "Loaded ${types.size} types for category $categoryId")
            
            _state.update { it.copy(vehicleTypes = types) }
            
            // If we have an initial vehicle type, try to match it in the loaded types
            _state.value.initialVehicle?.let { vehicle ->
                if (vehicle.type.VehicleCategoryId == categoryId) {
                    // Find matching type in loaded types
                    types.find { it.Id == vehicle.type.Id }?.let { matchingType ->
                        Log.d("EditVehicleViewModel", "Auto-selecting matching type: ${matchingType.Name}")
                        _state.update { it.copy(selectedType = matchingType) }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("EditVehicleViewModel", "Failed to load vehicle types for category $categoryId", e)
            _state.update { it.copy(error = "Failed to load vehicle types. Please try again.") }
        }
    }

    fun selectCategory(category: VehicleCategory) {
        viewModelScope.launch {
            // Store current selected type to check if we need to reselect it
            val currentTypeId = _state.value.selectedType?.Id
            val currentType = _state.value.selectedType
            
            _state.update { it.copy(
                selectedCategory = category,
                // Only reset type if the category actually changed
                selectedType = if (it.selectedCategory?.id != category.id) null else it.selectedType,
                // Only clear types if the category actually changed
                vehicleTypes = if (it.selectedCategory?.id != category.id) emptyList() else it.vehicleTypes
            ) }
            
            // Only load types if the category changed
            if (_state.value.selectedCategory?.id != category.id) {
                loadVehicleTypes(category.id)
            } else if (currentType != null && _state.value.selectedType == null) {
                // If we kept the same category but lost the type (shouldn't happen), reselect it
                _state.update { it.copy(selectedType = currentType) }
            }
        }
    }

    fun selectVehicleType(type: VehicleType) {
        val currentCategoryId = _state.value.selectedCategory?.id
        
        // If we have a selected category, ensure the type belongs to it
        if (currentCategoryId != null && type.VehicleCategoryId != currentCategoryId) {
            Log.w("EditVehicleViewModel", "Type ${type.Name} (category: ${type.VehicleCategoryId}) does not match selected category: $currentCategoryId")
            // Don't update the type if it doesn't match the category
            return
        }
        
        Log.d("EditVehicleViewModel", "Setting selected type to: ${type.Name}")
        _state.update { it.copy(selectedType = type) }
    }

    fun selectEnergySource(energySourceEnum: EnergySourceEnum) {
        _state.update { it.copy(selectedEnergySourceEnum = energySourceEnum) }
    }

    fun selectBusiness(businessId: String?) {
        // Only allow selection/deselection if user is admin
        if (_state.value.currentUserRole == UserRole.SYSTEM_OWNER || _state.value.currentUserRole == UserRole.SUPERADMIN) {
             _state.update { it.copy(selectedBusinessId = businessId) }
        } else {
             Log.w("EditVehicleViewModel", "Non-admin user tried to change business assignment.")
        }
    }
    
    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun saveChanges(
        codename: String,
        model: String,
        serialNumber: String,
        description: String,
        bestSuitedFor: String,
        photoModel: String,
        nextService: String
        // Selected category, type, energy source, business are taken from state
    ) {
        val currentState = _state.value
        val vehicleToUpdateId = currentState.vehicleId
        val initialVehicleData = currentState.initialVehicle

        // Basic validation
        if (vehicleToUpdateId == null || initialVehicleData == null) {
            _state.update { it.copy(error = "Vehicle data is missing. Cannot save.") }
            return
        }
        if (currentState.selectedCategory == null || currentState.selectedType == null || currentState.selectedEnergySourceEnum == null) {
            _state.update { it.copy(error = "Category, Type, and Energy Source must be selected.") }
            return
        }
        if (codename.isBlank() || model.isBlank() || serialNumber.isBlank()) {
            _state.update { it.copy(error = "Codename, Model, and Serial Number cannot be blank.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                // Log current state for debugging
                Log.d("EditVehicleViewModel", "Current state before save: " +
                    "selectedBusinessId=${currentState.selectedBusinessId}, " +
                    "initialBusinessId=${initialVehicleData.businessId}")
                
                // Explicitly determine business ID based on role and selection
                val effectiveBusinessId = when (currentState.currentUserRole) {
                    UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> {
                        // For admins, use the selected business ID (which could be null for unassigned)
                        currentState.selectedBusinessId.also {
                            Log.d("EditVehicleViewModel", "Admin user selected business ID: $it")
                        }
                    }
                    else -> {
                        // For regular users, use their current business ID
                        initialVehicleData.businessId.also {
                            Log.d("EditVehicleViewModel", "Regular user using business ID: $it")
                        }
                    }
                }
                
                // Construct the updated vehicle object from current state and input fields
                val updatedVehicle = initialVehicleData.copy(
                    codename = codename,
                    model = model,
                    serialNumber = serialNumber,
                    description = description,
                    bestSuitedFor = bestSuitedFor,
                    photoModel = photoModel,
                    nextService = nextService,
                    type = currentState.selectedType, // Use selected type from state
                    categoryId = currentState.selectedCategory.id, // Update category ID based on selection
                    energyType = currentState.selectedEnergySourceEnum.name.uppercase(), // Use selected energy source
                    businessId = effectiveBusinessId // Use explicitly determined business ID
                )

                Log.d("EditVehicleViewModel", "Updating vehicle with businessId: ${updatedVehicle.businessId}")
                
                // Determine which repository method to call based on user role
                val savedVehicle = when (currentState.currentUserRole) {
                    UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> {
                        Log.d("EditVehicleViewModel", "Calling updateVehicleGlobally with businessId: ${updatedVehicle.businessId}")
                        vehicleRepository.updateVehicleGlobally(vehicleToUpdateId, updatedVehicle)
                    }
                    else -> {
                        // For other users, businessId MUST be present
                        val userBusinessId = updatedVehicle.businessId ?: run {
                            Log.e("EditVehicleViewModel", "Business ID is required for non-admin user to update vehicle.")
                             _state.update { it.copy(isSaving = false, error = "Business context missing for update.") }
                            return@launch
                        }
                        Log.d("EditVehicleViewModel", "Calling updateVehicle for business $userBusinessId")
                        vehicleRepository.updateVehicle(userBusinessId, vehicleToUpdateId, updatedVehicle)
                    }
                }

                Log.d("EditVehicleViewModel", "Save successful for vehicle: ${savedVehicle.codename}, businessId: ${savedVehicle.businessId}")
                _state.update { it.copy(isSaving = false, saveSuccess = true, initialVehicle = savedVehicle) }

            } catch (e: Exception) {
                Log.e("EditVehicleViewModel", "Error saving vehicle changes", e)
                _state.update { it.copy(isSaving = false, error = "Failed to save changes: ${e.message}") }
            }
        }
    }

} 