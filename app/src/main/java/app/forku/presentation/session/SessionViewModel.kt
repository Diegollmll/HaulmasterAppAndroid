package app.forku.presentation.session

import SessionState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.AuthRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val authRepository: AuthRepository
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
                val session = vehicleRepository.startSession(vehicleId, checkId)
                _state.update { 
                    it.copy(
                        session = session,
                        isLoading = false,
                        error = null
                    )
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
                val currentSession = state.value.session
                if (currentSession == null) {
                    _state.update { 
                        it.copy(
                            error = "No active session to end",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                _state.update { it.copy(isLoading = true) }
                val endedSession = vehicleRepository.endSession(currentSession.id)
                _state.update { 
                    it.copy(
                        session = endedSession,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to end session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadCurrentSession() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val session = vehicleRepository.getCurrentSession()
                _state.update { 
                    it.copy(
                        session = session,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to load session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
} 