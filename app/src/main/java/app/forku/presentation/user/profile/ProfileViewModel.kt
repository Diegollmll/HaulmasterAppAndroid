package app.forku.presentation.user.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.model.session.VehicleSessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
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
                val localUser = userRepository.getCurrentUser()
                val user = if (localUser != null) {
                    // Try to refresh from API
                    val apiUser = userRepository.getUserById(localUser.id)
                    apiUser ?: localUser // fallback to local if API fails
                } else null
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
                    Log.d("ProfileViewModel", "Loaded operator data:")
                    Log.d("ProfileViewModel", "ID: ${operator.id}")
                    Log.d("ProfileViewModel", "Name: ${operator.firstName} ${operator.lastName}")
                    Log.d("ProfileViewModel", "Photo URL: ${operator.photoUrl}")
                    Log.d("ProfileViewModel", "Role: ${operator.role}")
                    updateProfileState(operator)
                } else {
                    Log.e("ProfileViewModel", "Operator not found for ID: $operatorId")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading operator profile", e)
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error logging out: ${e.message}") }
            }
        }
    }

    fun refreshProfile() {
        loadCurrentUserProfile()
    }

    private suspend fun updateProfileState(user: User) {
        Log.d("ProfileViewModel", "Updating profile state for user:")
        Log.d("ProfileViewModel", "ID: ${user.id}")
        Log.d("ProfileViewModel", "Name: ${user.firstName} ${user.lastName}")
        Log.d("ProfileViewModel", "Photo URL: ${user.photoUrl}")
        Log.d("ProfileViewModel", "Role: ${user.role}")
        Log.d("ProfileDebug", "Loaded user: ${user.username}, role: ${user.role}")
        // Get all sessions for the user
        val userSessions = vehicleSessionRepository.getSessionsByUserId(user.id)
        
        // Calculate total hours from completed sessions
        val totalHours = userSessions
            .filter { it.status == VehicleSessionStatus.NOT_OPERATING }
            .also { sessions ->
                Log.d("ProfileViewModel", "Found ${sessions.size} completed sessions")
                sessions.forEach { session ->
                    Log.d("ProfileViewModel", "Session ID: ${session.id}")
                    Log.d("ProfileViewModel", "Start Time: ${session.startTime}")
                    Log.d("ProfileViewModel", "End Time: ${session.endTime}")
                }
            }
            .sumOf { session ->
                try {
                    val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
                    Log.d("ProfileViewModel", "Processing session ${session.id}")
                    Log.d("ProfileViewModel", "Raw startTime: ${session.startTime}")
                    Log.d("ProfileViewModel", "Raw endTime: ${session.endTime}")
                    
                    val startTime = java.time.ZonedDateTime.parse(session.startTime, formatter).toInstant().toEpochMilli()
                    val endTime = session.endTime?.let { 
                        java.time.ZonedDateTime.parse(it, formatter).toInstant().toEpochMilli()
                    } ?: run {
                        Log.e("ProfileViewModel", "EndTime is null for session ${session.id}")
                        return@sumOf 0.0
                    }
                    
                    val duration = if (endTime > startTime) {
                        (endTime - startTime) / (1000.0 * 60 * 60) // Convert milliseconds to hours
                    } else 0.0
                    
                    Log.d("ProfileViewModel", "Session ${session.id} duration: $duration hours")
                    duration
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error calculating session duration for ${session.id}: ${e.message}")
                    Log.e("ProfileViewModel", "Stack trace: ${e.stackTraceToString()}")
                    0.0
                }
            }
        
        Log.d("ProfileViewModel", "Final total hours: $totalHours")
        
        // Get total incidents using backend count endpoint
        val totalIncidents = incidentRepository.getIncidentCountForUser(user.id)
        
        // Get active vehicle session if any
        val OPERATING = userSessions.find { it.status == VehicleSessionStatus.OPERATING }
        
        val activeVehicle = OPERATING?.let { session ->
            val businessId = user.businessId
            if (businessId != null) {
                vehicleRepository.getVehicle(session.vehicleId, businessId)
            } else {
                Log.e("ProfileViewModel", "No business context available for vehicle ${session.vehicleId}")
                null
            }
        }
        
        // Update user with current active status based on vehicle session
        val updatedUser = user.copy(
            isActive = OPERATING != null,
            totalHours = totalHours.toFloat(),
            sessionsCompleted = userSessions.count { it.status == VehicleSessionStatus.NOT_OPERATING },
            incidentsReported = totalIncidents
        )
        
        _state.update { currentState ->
            currentState.copy(
                user = updatedUser,
                currentSession = OPERATING,
                activeVehicle = activeVehicle,
                totalSessions = userSessions.size,
                totalIncidents = totalIncidents,
                isLoading = false,
                error = null
            )
        }
    }
} 