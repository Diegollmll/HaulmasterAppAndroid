package app.forku.presentation.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.model.session.SessionStatus
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

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val user = userRepository.getCurrentUser()
                user?.let { updateProfileState(it) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun loadOperatorProfile(operatorId: String) {
        viewModelScope.launch {
            try {
                val operator = userRepository.getUserById(operatorId)
                if (operator != null) {
                    android.util.Log.d("ProfileViewModel", "Loaded operator with photoUrl: ${operator.photoUrl}")
                    updateProfileState(operator)
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
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

    private suspend fun updateProfileState(user: User) {
        try {
            // Get total sessions
            val totalSessions = sessionRepository.getSessionsByUserId(user.id).size
            
            // Get total incidents
            val totalIncidents = incidentRepository.getIncidentsByUserId(user.id).getOrDefault(emptyList()).size
            
            // Get active vehicle session if any
            val activeSession = sessionRepository.getSessionsByUserId(user.id)
                .find { it.status == SessionStatus.ACTIVE }
            
            val activeVehicle = activeSession?.let { session ->
                vehicleRepository.getVehicle(session.vehicleId)
            }
            
            // Update user with current active status based on vehicle session
            val updatedUser = user.copy(isActive = activeSession != null)
            
            _state.update { currentState ->
                currentState.copy(
                    user = updatedUser,
                    currentSession = activeSession,
                    activeVehicle = activeVehicle,
                    totalSessions = totalSessions,
                    totalIncidents = totalIncidents,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }
} 