package app.forku.presentation.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreShiftCheckState(
    val id: String,
    val vehicleId: String,
    val vehicleCodename: String,
    val operatorName: String,
    val status: String,
    val lastCheckDateTime: String
)

data class AllChecklistState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val checks: List<PreShiftCheckState> = emptyList()
)

@HiltViewModel
class AllChecklistViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AllChecklistState())
    val state: StateFlow<AllChecklistState> = _state.asStateFlow()

    init {
        loadChecks()
    }

    fun loadChecks() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val checks = checklistRepository.getAllChecks()
                val checkStates = checks.mapNotNull { check ->
                    try {
                        val operator = userRepository.getUserById(check.userId)
                        val vehicle = vehicleRepository.getVehicle(check.vehicleId)
                        PreShiftCheckState(
                            id = check.id,
                            vehicleId = check.vehicleId,
                            vehicleCodename = vehicle.codename,
                            operatorName = operator?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                            status = check.status,
                            lastCheckDateTime = check.lastCheckDateTime
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.lastCheckDateTime }

                _state.value = _state.value.copy(
                    isLoading = false,
                    checks = checkStates,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error loading checks: ${e.message}"
                )
            }
        }
    }
} 