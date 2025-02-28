package app.forku.presentation.incident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.usecase.incident.ReportIncidentUseCase
import app.forku.domain.repository.session.SessionRepository
import app.forku.presentation.incident.model.IncidentFormSection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentReportState())
    val state = _state.asStateFlow()

    init {
        loadCurrentSession()
    }

    private fun loadCurrentSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getCurrentSession()
                _state.update { 
                    it.copy(
                        vehicleId = session?.vehicleId,
                        sessionId = session?.id
                    )
                }
            } catch (e: Exception) {
                // Session not required for incident reporting
                android.util.Log.w("Incident", "No active session", e)
            }
        }
    }

    fun setType(type: IncidentType) {
        _state.update { it.copy(type = type) }
    }

    fun setIncidentType(incidentType: String) {
        try {
            val type = IncidentType.valueOf(incidentType.uppercase().replace(" ", "_"))
            _state.update { it.copy(type = type) }
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(error = "Invalid incident type") }
        }
    }

    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun submitReport() {
        val currentState = state.value
        
        if (currentState.type == null) {
            _state.update { it.copy(error = "Please select an incident type") }
            return
        }

        if (currentState.description.isBlank()) {
            _state.update { it.copy(error = "Please provide a description") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                reportIncidentUseCase(
                    type = currentState.type,
                    description = currentState.description
                )
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isSubmitted = true,
                        showSuccessDialog = true
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to submit report"
                    )
                }
            }
        }
    }

    fun resetForm() {
        _state.value = IncidentReportState()
    }

    fun dismissSuccessDialog() {
        _state.update { it.copy(showSuccessDialog = false) }
    }

    fun updateState(newState: IncidentReportState) {
        _state.value = newState
    }

    fun nextSection() {
        val currentState = state.value
        val nextSection = when (currentState.currentSection) {
            IncidentFormSection.BasicInfo -> IncidentFormSection.PeopleInvolved
            IncidentFormSection.PeopleInvolved -> IncidentFormSection.VehicleInfo
            IncidentFormSection.VehicleInfo -> IncidentFormSection.IncidentDetails
            IncidentFormSection.IncidentDetails -> IncidentFormSection.RootCauseAnalysis
            IncidentFormSection.RootCauseAnalysis -> IncidentFormSection.Documentation
            IncidentFormSection.Documentation -> IncidentFormSection.Documentation // Stay on last section
        }
        _state.update { it.copy(currentSection = nextSection) }
    }

    fun previousSection() {
        val currentState = state.value
        val previousSection = when (currentState.currentSection) {
            IncidentFormSection.Documentation -> IncidentFormSection.RootCauseAnalysis
            IncidentFormSection.RootCauseAnalysis -> IncidentFormSection.IncidentDetails
            IncidentFormSection.IncidentDetails -> IncidentFormSection.VehicleInfo
            IncidentFormSection.VehicleInfo -> IncidentFormSection.PeopleInvolved
            IncidentFormSection.PeopleInvolved -> IncidentFormSection.BasicInfo
            IncidentFormSection.BasicInfo -> IncidentFormSection.BasicInfo // Stay on first section
        }
        _state.update { it.copy(currentSection = previousSection) }
    }
} 