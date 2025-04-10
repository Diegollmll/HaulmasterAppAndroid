package app.forku.presentation.admin.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminVehiclesListState(
    val isLoading: Boolean = false,
    val vehicles: List<Vehicle> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminVehiclesListViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
    // Add other dependencies like UserRepository if needed for filtering
) : ViewModel() {

    private val _state = MutableStateFlow(AdminVehiclesListState())
    val state: StateFlow<AdminVehiclesListState> = _state.asStateFlow()

    init {
        loadVehicles()
    }

    fun loadVehicles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch all vehicles using admin context (businessId = "0")
                // TODO: Implement pagination or filtering if needed for large lists
                val allVehicles = vehicleRepository.getAllVehicles() 
                _state.update { it.copy(isLoading = false, vehicles = allVehicles) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load vehicles") }
            }
        }
    }
} 