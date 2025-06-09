package app.forku.presentation.incident.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import app.forku.domain.model.incident.Incident
import app.forku.domain.model.incident.toDisplayText
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
                        Log.d("IncidentDetailVM", "Loaded incident from repo: $incident")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                incident = incident.toIncidentDetail().also { detail ->
                                    Log.d("IncidentDetailVM", "Mapped to IncidentDetail: $detail")
                                },
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
        type = type.toDisplayText(),
        description = description,
        date = date,
        location = location,
        locationDetails = locationDetails,
        weather = weather,
        severityLevel = severityLevel?.toString() ?: "Not specified",
        status = status.toString(),
        vehicleName = vehicleName,
        vehicleType = vehicleType?.toString() ?: "Not specified",
        isLoadCarried = isLoadCarried,
        loadBeingCarried = loadBeingCarried,
        loadWeight = loadWeight?.toString() ?: "Not specified",
        preshiftCheckStatus = preshiftCheckStatus,
        othersInvolved = othersInvolved,
        injuries = injuries,
        injuryLocations = injuryLocations,
        typeSpecificFields = typeSpecificFields,
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
    val locationDetails: String = "",
    val weather: String = "",
    val severityLevel: String = "",
    val status: String = "",
    val vehicleName: String = "",
    val vehicleType: String = "",
    val isLoadCarried: Boolean = false,
    val loadBeingCarried: String = "",
    val loadWeight: String = "",
    val preshiftCheckStatus: String = "",
    val othersInvolved: String? = null,
    val injuries: String = "",
    val injuryLocations: List<String> = emptyList(),
    val typeSpecificFields: app.forku.domain.model.incident.IncidentTypeFields? = null,
    val attachments: List<String> = emptyList()
) 