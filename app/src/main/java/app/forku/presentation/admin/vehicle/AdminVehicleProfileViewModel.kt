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

data class AdminVehicleProfileState(
    val isLoading: Boolean = false,
    val vehicle: Vehicle? = null,
    val error: String? = null
    // Add other state properties as needed (e.g., related data like sessions, checklists)
)

@HiltViewModel
class AdminVehicleProfileViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminVehicleProfileState())
    val state: StateFlow<AdminVehicleProfileState> = _state.asStateFlow()

    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, vehicle = null) }
            try {
                // Fetch the specific vehicle using admin context (businessId = "0")
                // This ensures we can load any vehicle regardless of its assigned business
                val vehicle = vehicleRepository.getVehicle(id = vehicleId, businessId = "0")
                _state.update { it.copy(isLoading = false, vehicle = vehicle) }
                // TODO: Load related data if necessary (sessions, checklists) using vehicle.businessId or "0"
            } catch (e: Exception) {
                 // Check if the error is "Vehicle not found" specifically
                 val errorMessage = if (e.message?.contains("not found", ignoreCase = true) == true) {
                     "Vehicle with ID $vehicleId not found."
                 } else {
                     e.message ?: "Failed to load vehicle details"
                 }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    // Add other functions for admin actions (e.g., updating status, navigating to edit)
} 