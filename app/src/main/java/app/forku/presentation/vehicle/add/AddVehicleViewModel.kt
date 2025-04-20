package app.forku.presentation.vehicle.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.model.vehicle.EnergySourceEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.model.user.UserRole
import app.forku.presentation.dashboard.Business

data class AddVehicleState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val vehicleTypes: List<VehicleType> = emptyList(),
    val selectedType: VehicleType? = null,
    val businesses: List<Business> = emptyList(),
    val selectedBusinessId: String? = null,
    val currentUserRole: UserRole? = null,
    val vehicleCategories: List<VehicleCategory> = emptyList(),
    val selectedCategory: VehicleCategory? = null,
    val energySourceEnums: List<EnergySourceEnum> = listOf(EnergySourceEnum.ELECTRIC, EnergySourceEnum.LPG, EnergySourceEnum.DIESEL),
    val selectedEnergySourceEnum: EnergySourceEnum? = null
)

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val vehicleTypeRepository: VehicleTypeRepository,
    private val businessRepository: BusinessRepository,
    private val vehicleCategoryRepository: VehicleCategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddVehicleState())
    val state: StateFlow<AddVehicleState> = _state

    init {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            _state.update { it.copy(currentUserRole = currentUser?.role) }
            
            // Load businesses for SYSTEM_OWNER and SUPERADMIN
            if (currentUser?.role == UserRole.SYSTEM_OWNER || currentUser?.role == UserRole.SUPERADMIN) {
                loadBusinesses()
            }
            
            // Load vehicle categories
            loadVehicleCategories()
        }
    }

    private suspend fun loadVehicleCategories() {
        try {
            _state.update { it.copy(isLoading = true) }
            val categories = vehicleCategoryRepository.getVehicleCategories()
            _state.update { it.copy(
                vehicleCategories = categories,
                isLoading = false
            ) }
        } catch (e: Exception) {
            _state.update { it.copy(
                error = "Failed to load vehicle categories: ${e.message}",
                isLoading = false
            ) }
        }
    }

    private suspend fun loadBusinesses() {
        try {
            _state.update { it.copy(isLoading = true) }
            val businesses = businessRepository.getAllBusinesses()
            _state.update { it.copy(
                businesses = businesses,
                isLoading = false
            ) }
        } catch (e: Exception) {
            _state.update { it.copy(
                error = "Failed to load businesses: ${e.message}",
                isLoading = false
            ) }
        }
    }

    fun selectBusiness(businessId: String) {
        _state.update { it.copy(selectedBusinessId = businessId) }
    }

    fun selectCategory(category: VehicleCategory) {
        viewModelScope.launch {
            try {
                Log.d("AddVehicleViewModel", "Selected category: ${category.name}, ID: ${category.id}")
                _state.update { it.copy(
                    isLoading = true,
                    selectedCategory = category,
                    selectedType = null, // Reset selected type when category changes
                    vehicleTypes = emptyList() // Clear existing types
                ) }
                
                val types = vehicleTypeRepository.getVehicleTypesByCategory(category.id)
                Log.d("AddVehicleViewModel", "Retrieved vehicle types: $types")
                _state.update { it.copy(
                    vehicleTypes = types,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Log.e("AddVehicleViewModel", "Error loading vehicle types", e)
                _state.update { it.copy(
                    error = "Failed to load vehicle types: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun selectEnergySource(energySourceEnum: EnergySourceEnum) {
        _state.update { it.copy(selectedEnergySourceEnum = energySourceEnum) }
    }

    fun selectVehicleType(type: VehicleType) {
        if (type.categoryId == _state.value.selectedCategory?.id) {
            _state.update { it.copy(selectedType = type) }
        }
    }

    fun addVehicle(
        codename: String,
        model: String,
        description: String,
        bestSuitedFor: String,
        photoModel: String,
        nextService: String,
        type: VehicleType,
        serialNumber: String
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Get current user
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    ) }
                    return@launch
                }

                // Determine business ID based on role
                val businessId: String? = when (currentUser.role) {
                    UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> {
                        // Business ID is optional for these roles
                        _state.value.selectedBusinessId
                    }
                    else -> currentUser.businessId ?: run {
                        _state.update { it.copy(
                            isLoading = false,
                            error = "No business context available"
                        ) }
                        return@launch
                    }
                }

                // Check if energy source is selected
                val energySource = _state.value.selectedEnergySourceEnum ?: run {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Please select an energy source"
                    ) }
                    return@launch
                }

                // Create vehicle
                Log.d("AddVehicleViewModel", "Creating vehicle: codename=$codename, model=$model, type=$type, businessId=$businessId")
                
                vehicleRepository.createVehicle(
                    codename = codename,
                    model = model,
                    description = description,
                    bestSuitedFor = bestSuitedFor,
                    photoModel = photoModel,
                    energyType = energySource.name,
                    nextService = nextService,
                    type = type,
                    businessId = businessId,
                    serialNumber = serialNumber
                )

                _state.update { it.copy(
                    isLoading = false,
                    success = true
                ) }
            } catch (e: Exception) {
                Log.e("AddVehicleViewModel", "Error creating vehicle", e)
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create vehicle"
                ) }
            }
        }
    }
} 