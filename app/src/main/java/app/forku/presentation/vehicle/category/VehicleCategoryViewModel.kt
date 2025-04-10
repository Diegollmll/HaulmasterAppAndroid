package app.forku.presentation.vehicle.category

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.repository.vehicle.VehicleCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleCategoryViewModel @Inject constructor(
    private val repository: VehicleCategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleCategoryState())
    val state = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val categories = repository.getVehicleCategories()
                _state.value = _state.value.copy(
                    categories = categories,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("VehicleCategoryVM", "Error loading categories", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load categories"
                )
            }
        }
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _state.value = _state.value.copy(showAddDialog = false)
    }

    fun showEditDialog(category: VehicleCategory) {
        _state.value = _state.value.copy(
            showEditDialog = true,
            selectedCategory = category
        )
    }

    fun hideEditDialog() {
        _state.value = _state.value.copy(
            showEditDialog = false,
            selectedCategory = null
        )
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                repository.createVehicleCategory(name)
                loadCategories()
                hideAddDialog()
            } catch (e: Exception) {
                Log.e("VehicleCategoryVM", "Error creating category", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create category"
                )
            }
        }
    }

    fun updateCategory(category: VehicleCategory) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                repository.updateVehicleCategory(
                    id = category.id,
                    name = category.name,
                    description = category.description
                )
                loadCategories()
                hideEditDialog()
            } catch (e: Exception) {
                Log.e("VehicleCategoryVM", "Error updating category", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update category"
                )
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                repository.deleteVehicleCategory(id)
                loadCategories()
            } catch (e: Exception) {
                Log.e("VehicleCategoryVM", "Error deleting category", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete category"
                )
            }
        }
    }
} 