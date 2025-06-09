package app.forku.presentation.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.api.dto.safetyalert.SafetyAlertDto
import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SafetyAlertsViewModel @Inject constructor(
    private val repository: SafetyAlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SafetyAlertsState())
    val state: StateFlow<SafetyAlertsState> = _state.asStateFlow()

    init {
        loadSafetyAlerts()
    }

    fun loadSafetyAlerts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val alerts = repository.getSafetyAlertList()
                _state.update { 
                    it.copy(
                        alerts = alerts.map { dto ->
                            SafetyAlert(
                                id = dto.id,
                                title = "Safety Alert #${dto.id.take(8)}", // Generate a title from the ID
                                description = "Vehicle: ${dto.vehicleId}\nChecklist Item: ${dto.answeredChecklistItemId}",
                                createdAt = dto.creationDateTime ?: "",
                                updatedAt = dto.creationDateTime ?: ""
                            )
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Error loading safety alerts",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createSafetyAlert(answeredChecklistItemId: String, vehicleId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val alert = SafetyAlertDto(
                    id = java.util.UUID.randomUUID().toString(),  // Generate a new GUID
                    answeredChecklistItemId = answeredChecklistItemId,
                    goUserId = "",  // Required by API
                    vehicleId = vehicleId,
                    isDirty = false,  // Required by API
                    isNew = true,  // Required by API
                    isMarkedForDeletion = false,  // Required by API
                    creationDateTime = java.time.Instant.now().toString() // Nuevo campo
                )
                
                val savedAlert = repository.saveSafetyAlert(alert)
                if (savedAlert != null) {
                    _state.update { currentState ->
                        currentState.copy(
                            alerts = currentState.alerts + SafetyAlert(
                                id = savedAlert.id,
                                title = "Safety Alert #${savedAlert.id.take(8)}",
                                description = "Vehicle: ${savedAlert.vehicleId}\nChecklist Item: ${savedAlert.answeredChecklistItemId}",
                                createdAt = java.time.Instant.now().toString(),
                                updatedAt = java.time.Instant.now().toString()
                            ),
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { 
                        it.copy(
                            error = "Failed to create safety alert",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Error creating safety alert",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteSafetyAlert(alert: SafetyAlert) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val dto = SafetyAlertDto(
                    id = alert.id,
                    answeredChecklistItemId = "",  // Required by API
                    goUserId = "",  // Required by API
                    vehicleId = "",  // Required by API
                    isDirty = false,  // Required by API
                    isNew = false,  // Required by API
                    isMarkedForDeletion = true  // Required by API
                )
                
                val success = repository.deleteSafetyAlert(dto)
                if (success) {
                    _state.update { currentState ->
                        currentState.copy(
                            alerts = currentState.alerts.filter { it.id != alert.id },
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { 
                        it.copy(
                            error = "Failed to delete safety alert",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Error deleting safety alert",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 