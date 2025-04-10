package app.forku.presentation.vehicle.type

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleTypeViewModel @Inject constructor(
    private val typeRepository: VehicleTypeRepository,
    private val categoryRepository: VehicleCategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleTypeState())
    val state: StateFlow<VehicleTypeState> = _state

    init {
        loadCategories()
        loadTypes()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                val categories = categoryRepository.getVehicleCategories()
                _state.update { it.copy(
                    categories = categories,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to load categories: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                val types = typeRepository.getVehicleTypes()
                _state.update { it.copy(
                    types = types,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to load types: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun loadTypesByCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = true,
                    error = null,
                    selectedCategoryId = categoryId
                ) }
                val types = typeRepository.getVehicleTypesByCategory(categoryId)
                _state.update { it.copy(
                    types = types,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to load types for category: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun showAddDialog() {
        _state.update { it.copy(
            showAddDialog = true,
            selectedType = null
        ) }
    }

    fun hideAddDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(type: VehicleType) {
        _state.update { it.copy(
            showEditDialog = true,
            selectedType = type
        ) }
    }

    fun hideEditDialog() {
        _state.update { it.copy(
            showEditDialog = false,
            selectedType = null
        ) }
    }

    fun addType(name: String, categoryId: String, requiresCertification: Boolean) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                typeRepository.createVehicleType(
                    name = name,
                    categoryId = categoryId,
                    requiresCertification = requiresCertification
                )
                hideAddDialog()
                loadTypes()
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to add type: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun updateType(type: VehicleType) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                typeRepository.updateVehicleType(
                    id = type.id,
                    name = type.name,
                    categoryId = type.categoryId,
                    requiresCertification = type.requiresCertification
                )
                hideEditDialog()
                loadTypes()
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to update type: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun deleteType(typeId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                typeRepository.deleteVehicleType(typeId)
                loadTypes()
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to delete type: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
} 