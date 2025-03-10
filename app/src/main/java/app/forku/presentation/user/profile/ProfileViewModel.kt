package app.forku.presentation.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository
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
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
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
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Get current user first
                val currentUser = userRepository.getCurrentUser() ?: run {
                    _state.update {
                        it.copy(
                            error = "No hay usuario autenticado",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Try to refresh user data from API
                val refreshResult = userRepository.refreshCurrentUser()
                val updatedUser = refreshResult.getOrNull() ?: currentUser

                val currentSession = sessionRepository.getCurrentSession()
                val activeVehicle = currentSession?.let {
                    vehicleRepository.getVehicle(it.vehicleId)
                }

                _state.update {
                    it.copy(
                        user = updatedUser,
                        currentSession = currentSession,
                        activeVehicle = activeVehicle,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al cargar perfil: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
} 