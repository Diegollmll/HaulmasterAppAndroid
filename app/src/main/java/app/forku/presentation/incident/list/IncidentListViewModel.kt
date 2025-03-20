package app.forku.presentation.incident.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.incident.toDisplayText
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.model.user.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentListViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(IncidentHistoryState())
    val state = _state.asStateFlow()

    init {
        // Remove automatic loading here as we'll load based on parameters
    }

    fun loadIncidents(userId: String? = null, source: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "User not authenticated"
                        )
                    }
                    return@launch
                }

                val isAdmin = currentUser.role == UserRole.ADMIN
                
                // Determine which incidents to load based on source and parameters
                val incidentsResult = when {
                    // From dashboard and user is admin -> show all incidents
                    source == null && isAdmin -> {
                        android.util.Log.d("Incidents", "Admin loading all incidents from dashboard")
                        incidentRepository.getIncidents()
                    }
                    // From profile with specific userId -> show that user's incidents
                    source == "profile" && userId != null -> {
                        android.util.Log.d("Incidents", "Loading incidents for user: $userId")
                        incidentRepository.getIncidentsByUserId(userId)
                    }
                    // From profile without userId or any other case -> show current user's incidents
                    else -> {
                        android.util.Log.d("Incidents", "Loading incidents for current user")
                        incidentRepository.getOperatorIncidents()
                    }
                }

                incidentsResult
                    .onSuccess { incidents ->
                        android.util.Log.d("Incidents", "Received ${incidents.size} incidents")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                incidents = incidents
                                    .map { incident ->
                                        android.util.Log.d("Incidents", "Mapping incident: ${incident.id}")
                                        IncidentItem(
                                            id = incident.id ?: "",
                                            type = incident.type.toDisplayText(),
                                            description = incident.description,
                                            date = incident.timestamp,
                                            status = incident.status.toString(),
                                            vehicleName = incident.vehicleName,
                                            creatorName = getUserName(incident.userId)
                                        )
                                    }
                                    .sortedByDescending { incident -> incident.date }
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

    private suspend fun getUserName(userId: String): String {
        return try {
            userRepository.getUserById(userId)?.fullName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
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
    val vehicleName: String,
    val creatorName: String = "Unknown"
) 