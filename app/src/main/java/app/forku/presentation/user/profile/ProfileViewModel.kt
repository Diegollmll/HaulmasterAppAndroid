package app.forku.presentation.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.incident.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val vehicleRepository: VehicleRepository,
    private val incidentRepository: IncidentRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al cerrar sesión: ${e.message}") }
            }
        }
    }

    fun refreshProfile() {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null) {
                    updateProfileState(currentUser)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun loadOperatorProfile(operatorId: String) {
        viewModelScope.launch {
            try {
                val operator = userRepository.getUserById(operatorId)
                if (operator != null) {
                    updateProfileState(operator)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private suspend fun updateProfileState(user: User) {
        try {
            // Get user's sessions
            val sessions = sessionRepository.getSessionsByUserId(user.id)
            
            // Get user's incidents
            val incidentsResult = incidentRepository.getIncidentsByUserId(user.id)
            val incidents = incidentsResult.getOrDefault(emptyList())
            
            _state.update { it.copy(
                user = user,
                totalSessions = sessions.size,
                totalIncidents = incidents.size,
                isLoading = false,
                error = null
            ) }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }
} 