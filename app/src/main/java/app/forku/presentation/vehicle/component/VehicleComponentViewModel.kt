package app.forku.presentation.vehicle.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.api.dto.vehicle.VehicleComponentDto
import app.forku.domain.repository.vehicle.VehicleComponentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class VehicleComponentState(
    val components: List<VehicleComponentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VehicleComponentViewModel @Inject constructor(
    private val repository: VehicleComponentRepository
) : ViewModel() {

    private val TAG = "VehicleComponentVM"
    private val _state = MutableStateFlow(VehicleComponentState())
    val state: StateFlow<VehicleComponentState> = _state.asStateFlow()

    init {
        loadComponents()
    }

    fun loadComponents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.getAllComponents()
                if (response.isSuccessful) {
                    val components = response.body() ?: emptyList()
                    // Filter out any components with null or blank names
                    val validComponents = components.filter { component ->
                        !component.name.isNullOrBlank()
                    }
                    Log.d(TAG, "Successfully loaded ${validComponents.size} valid components out of ${components.size} total")
                    _state.update { it.copy(components = validComponents, isLoading = false) }
                } else {
                    val error = "Failed to load components: ${response.message()}"
                    Log.e(TAG, error)
                    _state.update { it.copy(error = error, isLoading = false) }
                }
            } catch (e: Exception) {
                val error = e.message ?: "Unknown error"
                Log.e(TAG, "Error loading components: $error", e)
                _state.update { it.copy(error = error, isLoading = false) }
            }
        }
    }

    fun createComponent(name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val component = VehicleComponentDto(
                    id = null,
                    name = name,
                    description = description
                )
                val response = repository.createComponent(component)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully created component: $name")
                    loadComponents() // Reload the list after creating
                } else {
                    val error = "Failed to create component: ${response.message()}"
                    Log.e(TAG, error)
                    _state.update { it.copy(error = error, isLoading = false) }
                }
            } catch (e: Exception) {
                val error = e.message ?: "Unknown error"
                Log.e(TAG, "Error creating component: $error", e)
                _state.update { it.copy(error = error, isLoading = false) }
            }
        }
    }

    fun updateComponent(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val component = VehicleComponentDto(
                    id = id,
                    name = name,
                    description = description
                )
                val response = repository.updateComponent(id, component)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully updated component: $id")
                    loadComponents() // Reload the list after updating
                } else {
                    val error = "Failed to update component: ${response.message()}"
                    Log.e(TAG, error)
                    _state.update { it.copy(error = error, isLoading = false) }
                }
            } catch (e: Exception) {
                val error = e.message ?: "Unknown error"
                Log.e(TAG, "Error updating component: $error", e)
                _state.update { it.copy(error = error, isLoading = false) }
            }
        }
    }

    fun deleteComponent(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.deleteComponent(id)
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully deleted component: $id")
                    loadComponents() // Reload the list after deleting
                } else {
                    val error = "Failed to delete component: ${response.message()}"
                    Log.e(TAG, error)
                    _state.update { it.copy(error = error, isLoading = false) }
                }
            } catch (e: Exception) {
                val error = e.message ?: "Unknown error"
                Log.e(TAG, "Error deleting component: $error", e)
                _state.update { it.copy(error = error, isLoading = false) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 