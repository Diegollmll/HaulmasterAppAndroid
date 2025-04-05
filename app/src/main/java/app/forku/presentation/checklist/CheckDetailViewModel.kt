package app.forku.presentation.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckDetailViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CheckDetailState())
    val state = _state.asStateFlow()

    fun loadCheckDetail(checkId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId
                
                if (businessId == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "No business context available"
                    )
                    return@launch
                }

                val check = checklistRepository.getCheckById(checkId)
                check?.let {
                    val operator = userRepository.getUserById(it.userId)
                    val vehicle = vehicleRepository.getVehicle(it.vehicleId, businessId)
                    _state.value = _state.value.copy(
                        check = PreShiftCheckState(
                            id = it.id,
                            vehicleId = it.vehicleId,
                            vehicleCodename = vehicle.codename,
                            operatorName = operator?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                            status = it.status,
                            lastCheckDateTime = it.lastCheckDateTime
                        ),
                        isLoading = false,
                        error = null
                    )
                } ?: run {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Check not found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error loading check: ${e.message}"
                )
            }
        }
    }
} 