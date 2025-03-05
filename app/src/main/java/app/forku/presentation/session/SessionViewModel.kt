package app.forku.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.session.SessionRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
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
                val session = sessionRepository.startSession(vehicleId, checkId)
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
                val endedSession = sessionRepository.endSession(currentSession.id)
                _state.update { 
                    it.copy(
                        session = null,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun loadCurrentSession() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val session = sessionRepository.getCurrentSession()
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
                        error = "Failed to load current session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.update { SessionState() }
    }
} 