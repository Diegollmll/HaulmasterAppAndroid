package app.forku.presentation.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckDetailViewModel @Inject constructor(
    private val checklistAnswerRepository: ChecklistAnswerRepository,
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
                val businessId = currentUser?.businessId ?: app.forku.core.Constants.BUSINESS_ID

                val answer = checklistAnswerRepository.getById(checkId)
                if (answer == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Check not found"
                    )
                    return@launch
                }
                val operator = userRepository.getUserById(answer.goUserId)
                val vehicle = vehicleRepository.getVehicle(answer.vehicleId, businessId)
                val operatorName = when {
                    operator == null -> "Desconocido"
                    !operator.firstName.isNullOrBlank() || !operator.lastName.isNullOrBlank() ->
                        "${operator.firstName.orEmpty()} ${operator.lastName.orEmpty()}".trim()
                    !operator.username.isNullOrBlank() -> operator.username
                    else -> "Desconocido"
                }
                _state.value = _state.value.copy(
                    check = PreShiftCheckState(
                        id = answer.id,
                        vehicleId = answer.vehicleId,
                        vehicleCodename = vehicle.codename,
                        operatorName = operatorName,
                        status = answer.status.toString(),
                        lastCheckDateTime = answer.lastCheckDateTime.takeIf { it.isNotBlank() }
                    ),
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error loading check: ${e.message}"
                )
            }
        }
    }
} 