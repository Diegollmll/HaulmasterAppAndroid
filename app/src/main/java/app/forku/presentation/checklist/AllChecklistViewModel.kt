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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreShiftCheckState(
    val id: String,
    val vehicleId: String,
    val vehicleCodename: String,
    val operatorName: String,
    val status: String,
    val lastCheckDateTime: String? = null
)

data class AllChecklistState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val checks: List<PreShiftCheckState> = emptyList(),
    val currentPage: Int = 1,
    val itemsPerPage: Int = 10,
    val hasMoreItems: Boolean = true
)

@HiltViewModel
class AllChecklistViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AllChecklistState())
    val state = _state.asStateFlow()

    init {
        loadChecks()
    }

    fun loadNextPage() {
        if (_state.value.isLoadingMore || !_state.value.hasMoreItems) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            loadChecks(
                page = _state.value.currentPage + 1,
                append = true
            )
        }
    }

    fun loadChecks(
        page: Int = 1,
        append: Boolean = false
    ) {
        viewModelScope.launch {
            if (!append) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            
            try {
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId
                
                if (businessId == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = "No business context available"
                        )
                    }
                    return@launch
                }
                
                val checks = checklistRepository.getAllChecks(page)
                val checkStates = checks.mapNotNull { check ->
                    try {
                        val operator = userRepository.getUserById(check.userId)
                        val vehicle = vehicleRepository.getVehicle(check.vehicleId, businessId)
                        PreShiftCheckState(
                            id = check.id,
                            vehicleId = check.vehicleId,
                            vehicleCodename = vehicle.codename,
                            operatorName = operator?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                            status = check.status,
                            lastCheckDateTime = check.lastCheckDateTime
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AllChecklistViewModel", "Error processing check: ${e.message}")
                        null
                    }
                }
                
                _state.update { currentState ->
                    val updatedChecks = if (append) {
                        currentState.checks + checkStates
                    } else {
                        checkStates
                    }
                    
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        checks = updatedChecks,
                        currentPage = if (checkStates.isNotEmpty()) page else currentState.currentPage,
                        hasMoreItems = checkStates.size >= currentState.itemsPerPage
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "Failed to load checks: ${e.message}"
                    )
                }
            }
        }
    }
} 