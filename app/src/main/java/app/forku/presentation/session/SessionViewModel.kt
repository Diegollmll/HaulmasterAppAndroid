package app.forku.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.session.StartVehicleSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val startVehicleSessionUseCase: StartVehicleSessionUseCase,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state = _state.asStateFlow()

    init {
        loadCurrentSession()
    }

    fun startSession(vehicleId: String, checkId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                startVehicleSessionUseCase(vehicleId, checkId)
                    .onSuccess { session ->
                        _state.update { 
                            it.copy(
                                session = session,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _state.update { 
                            it.copy(
                                error = "Failed to start session: ${error.message}",
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to start session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun endSession() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val currentSession = state.value.session
                if (currentSession != null) {
                    val endedSession = vehicleSessionRepository.endSession(currentSession.id)
                    _state.update { 
                        it.copy(
                            session = endedSession,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    throw Exception("No hay sesión activa para finalizar")
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error al finalizar sesión: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadCurrentSession() {
        viewModelScope.launch {
            try {
                val session = vehicleSessionRepository.getCurrentSession()
                _state.update { 
                    it.copy(
                        session = session,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to load session: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.update { SessionState() }
    }
} 