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
class IncidentHistoryViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {
    private val _state = MutableStateFlow(IncidentHistoryState())
    val state = _state.asStateFlow()

    init {
        loadIncidents()
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                android.util.Log.d("Incidents", "Fetching operator incidents")
                incidentRepository.getOperatorIncidents()
                    .onSuccess { incidents ->
                        android.util.Log.d("Incidents", "Received ${incidents.size} incidents")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                incidents = incidents.map { incident ->
                                    android.util.Log.d("Incidents", "Mapping incident: ${incident.id}")
                                    IncidentItem(
                                        id = incident.id ?: "",
                                        type = incident.type.toString(),
                                        description = incident.description,
                                        date = incident.timestamp,
                                        status = incident.status.toString(),
                                        vehicleName = incident.vehicleName
                                    )
                                }
                            )
                        }
                    }
                    .onFailure { error ->
                        android.util.Log.e("Incidents", "Error loading incidents", error)
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load incidents"
                            )
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("Incidents", "Exception in loadIncidents", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load incidents"
                    )
                }
            }
        }
    }
}

data class IncidentHistoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val incidents: List<IncidentItem> = emptyList()
)

data class IncidentItem(
    val id: String,
    val type: String,
    val description: String,
    val date: String,
    val status: String,
    val vehicleName: String
) 