package app.forku.presentation.incident.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import app.forku.domain.model.incident.Incident
import app.forku.domain.repository.incident.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentDetailViewModel @Inject constructor(
    private val repository: IncidentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentDetailState())
    val state: StateFlow<IncidentDetailState> = _state.asStateFlow()

    fun loadIncidentDetail(incidentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val incidentResult = repository.getIncidentById(incidentId)
                incidentResult.fold(
                    onSuccess = { incident ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                incident = incident.toIncidentDetail(),
                                error = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun Incident.toIncidentDetail() = IncidentDetail(
        id = id ?: "",
        type = type.name,
        description = description,
        date = date,
        location = location,
        attachments = photos.map { it.toString() }
    )
}

data class IncidentDetailState(
    val isLoading: Boolean = false,
    val incident: IncidentDetail? = null,
    val error: String? = null
)

data class IncidentDetail(
    val id: String,
    val type: String,
    val description: String,
    val date: Long,
    val location: String = "",
    val attachments: List<String> = emptyList()
) 