package app.forku.presentation.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
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
    private val checklistAnswerRepository: ChecklistAnswerRepository
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
                // Use real server-side pagination
                val answers = checklistAnswerRepository.getAllPaginated(page, _state.value.itemsPerPage)
                val checkStates = answers.map { answer ->
                    PreShiftCheckState(
                        id = answer.id,
                        vehicleId = answer.vehicleId,
                        vehicleCodename = answer.vehicleName,
                        operatorName = answer.operatorName,
                        status = answer.status.toString(),
                        lastCheckDateTime = answer.lastCheckDateTime.takeIf { it.isNotBlank() }
                    )
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
                        // If we received fewer items than requested, we've reached the end
                        hasMoreItems = checkStates.size == currentState.itemsPerPage
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