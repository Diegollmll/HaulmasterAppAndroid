package app.forku.presentation.incident.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.incident.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentListViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {
    private val _state = MutableStateFlow(IncidentListState())
    val state = _state.asStateFlow()

    init {
        loadIncidents()
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val result = incidentRepository.getIncidents()
                result.onSuccess { incidents ->
                    _state.update { 
                        it.copy(
                            incidents = incidents,
                            isLoading = false,
                            error = null
                        )
                    }
                }.onFailure { error ->
                    _state.update { 
                        it.copy(
                            error = error.message ?: "Failed to load incidents",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to load incidents",
                        isLoading = false
                    )
                }
            }
        }
    }
} 