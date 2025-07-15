package app.forku.presentation.incident.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.incident.toDisplayText
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.model.user.UserRole
import app.forku.core.business.BusinessContextManager
import app.forku.domain.repository.user.UserPreferencesRepository
import app.forku.presentation.common.components.BusinessContextUpdater
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentListViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val userRepository: UserRepository,
    override val businessContextManager: BusinessContextManager,
    override val userPreferencesRepository: UserPreferencesRepository
) : ViewModel(), BusinessContextUpdater {
    private val _state = MutableStateFlow(IncidentHistoryState())
    val state = _state.asStateFlow()
    
    // ✅ Expose current user for UI to determine admin features
    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
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
                            error = "Authentication required. Please log in again."
                        )
                    }
                    return@launch
                }

                val isAdmin = currentUser.role == UserRole.ADMIN
                
                // Determine which incidents to load based on source and parameters
                val incidentsResult = when {
                    // From dashboard and user is admin -> show all incidents
                    source == null && isAdmin -> {
                        Log.d("Incidents", "Admin loading all incidents from dashboard")
                        incidentRepository.getIncidents(include = "GOUser")
                    }
                    // From profile with specific userId -> show that user's incidents
                    source == "profile" && userId != null -> {
                        Log.d("Incidents", "Loading incidents for user: $userId")
                        incidentRepository.getIncidentsByUserId(userId)
                    }
                    // From profile without userId or any other case -> show current user's incidents
                    else -> {
                        Log.d("Incidents", "Loading incidents for current user")
                        incidentRepository.getOperatorIncidents()
                    }
                }

                incidentsResult
                    .onSuccess { incidents ->
                        Log.d("Incidents", "Received ${incidents.size} incidents with included user data")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                incidents = incidents
                                    .map { incident ->
                                        Log.d("Incidents", "Mapping incident: ${incident.id}, Creator: ${incident.creatorName}")
                                        IncidentItem(
                                            id = incident.id ?: "",
                                            type = incident.type.toDisplayText(),
                                            description = incident.description,
                                            date = incident.date,
                                            status = incident.status.toString(),
                                            vehicleName = incident.vehicleName,
                                            creatorName = incident.creatorName
                                        )
                                    }
                                    .sortedByDescending { incident -> incident.date }
                            )
                        }
                    }
                    .onFailure { error ->
                        Log.e("Incidents", "Error loading incidents", error)
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load incidents"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("Incidents", "Exception in loadIncidents", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load incidents"
                    )
                }
            }
        }
    }
    
    /**
     * Implementation of BusinessContextUpdater interface
     * Reloads incidents when context changes
     */
    override fun reloadData() {
        loadIncidents()
    }
    
    /**
     * ✅ NEW: Load incidents with temporary filters (VIEW_FILTER mode)
     * Does NOT change user's personal context/preferences
     * Used for admin filtering across different sites
     */
    fun loadIncidentsWithFilters(filterBusinessId: String?, filterSiteId: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Authentication required. Please log in again."
                        )
                    }
                    return@launch
                }

                // ✅ Use explicit filter parameters ONLY, never user context if global filter is defined
                val businessId = filterBusinessId ?: businessContextManager.getCurrentBusinessId()
                // If filterSiteId is null, it means "All Sites" is selected
                val siteId = filterSiteId

                Log.d("IncidentListViewModel", "🔧 VIEW_FILTER: Loading incidents with filters - businessId: $businessId, siteId: $siteId (null = All Sites)")

                // Determine which incidents to load based on filters
                val incidentsResult = when {
                    // Admin with filters -> show incidents based on business/site filters
                    businessId != null -> {
                        Log.d("IncidentListViewModel", "🔧 VIEW_FILTER: Admin loading incidents with filters")
                        if (siteId != null) {
                            // Specific site filter
                            Log.d("IncidentListViewModel", "🎯 Loading incidents for specific site: $siteId")
                            incidentRepository.getIncidentsWithFilters(businessId, siteId)
                        } else {
                            // "All Sites" filter
                            Log.d("IncidentListViewModel", "🎯 Loading incidents for all sites in business: $businessId")
                            incidentRepository.getIncidentsWithFilters(businessId, null)
                        }
                    }
                    // Fallback to current user's incidents
                    else -> {
                        Log.d("IncidentListViewModel", "👤 Fallback: Loading incidents for current user")
                        incidentRepository.getOperatorIncidents()
                    }
                }

                incidentsResult
                    .onSuccess { incidents ->
                        Log.d("IncidentListViewModel", "🔧 VIEW_FILTER: Received ${incidents.size} incidents with filters")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                incidents = incidents
                                    .map { incident ->
                                        Log.d("IncidentListViewModel", "Mapping incident: ${incident.id}, Creator: ${incident.creatorName}")
                                        IncidentItem(
                                            id = incident.id ?: "",
                                            type = incident.type.toDisplayText(),
                                            description = incident.description,
                                            date = incident.date,
                                            status = incident.status.toString(),
                                            vehicleName = incident.vehicleName,
                                            creatorName = incident.creatorName
                                        )
                                    }
                                    .sortedByDescending { incident -> incident.date }
                            )
                        }
                    }
                    .onFailure { error ->
                        Log.e("IncidentListViewModel", "🔧 VIEW_FILTER: Error loading incidents with filters", error)
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load incidents"
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("IncidentListViewModel", "🔧 VIEW_FILTER: Exception in loadIncidentsWithFilters", e)
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
    val date: Long?,
    val status: String,
    val vehicleName: String,
    val creatorName: String = "Unknown"
) 