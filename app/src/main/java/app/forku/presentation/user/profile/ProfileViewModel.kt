package app.forku.presentation.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.Operator
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun refreshProfile() {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Force refresh user data from API
                val currentUser = authRepository.refreshCurrentUser()
                val currentSession = sessionRepository.getCurrentSession()
                val activeVehicle = currentSession?.let {
                    vehicleRepository.getVehicle(it.vehicleId)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        user = currentUser,
                        activeVehicle = activeVehicle,
                        operator = currentUser?.let { user ->
                            Operator(
                                user = user,
                                name = user.name,
                                experienceLevel = "Rookie", // This should come from backend
                                points = 150,
                                totalHours = 890.1f,
                                totalDistance = 13212,
                                tasksCompleted = 124,
                                incidentsReported = 5,
                                lastMedicalCheck = user.lastMedicalCheck
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load profile"
                    )
                }
            }
        }
    }
} 