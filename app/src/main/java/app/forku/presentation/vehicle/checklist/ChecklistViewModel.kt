package app.forku.presentation.vehicle.checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.repository.VehicleRepositoryImpl
import app.forku.domain.model.Answer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val repository: VehicleRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChecklistState())
    val state = _state.asStateFlow()

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    init {
        loadVehicleAndChecklist()
    }

    fun loadVehicleAndChecklist() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val vehicle = repository.getVehicleById(vehicleId)
                val checkItems = repository.getChecklistItems(vehicleId)[0].items

                _state.update { 
                    it.copy(
                        vehicle = vehicle,
                        checkItems = checkItems,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to load checklist",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateItemResponse(id: String, answer: Boolean) {
        val currentItems = state.value.checkItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == id }
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(userAnswer = if (answer) Answer.PASS else Answer.FAIL)
            _state.update { it.copy(checkItems = currentItems) }
        }
    }

    fun submitCheck() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            try {
                repository.submitPreShiftCheck(
                    vehicleId = vehicleId,
                    checkItems = state.value.checkItems
                )
                _state.update { 
                    it.copy(
                        isSubmitting = false,
                        isCompleted = true
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to submit check",
                        isSubmitting = false
                    )
                }
            }
        }
    }
} 