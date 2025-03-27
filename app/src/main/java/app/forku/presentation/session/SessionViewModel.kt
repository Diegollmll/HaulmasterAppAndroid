package app.forku.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.session.VehicleSessionClosedMethod
import app.forku.domain.model.user.UserRole
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

    private val _canEndSession = MutableStateFlow<Boolean>(false)
    val canEndSession = _canEndSession.asStateFlow()

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

    fun endSession(sessionId: String? = null, isAdminClosure: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val targetSessionId = sessionId ?: state.value.session?.id
                if (targetSessionId != null) {
                    val closeMethod = if (isAdminClosure) {
                        VehicleSessionClosedMethod.ADMIN_CLOSED
                    } else {
                        VehicleSessionClosedMethod.USER_CLOSED
                    }
                    
                    val endedSession = vehicleSessionRepository.endSession(
                        sessionId = targetSessionId,
                        closeMethod = closeMethod
                    )
                    _state.update { 
                        it.copy(
                            session = endedSession,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    throw Exception("No active session to end")
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error ending session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun checkCanEndSession(sessionUserId: String) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            _canEndSession.value = currentUser?.let { user ->
                // User can end their own session or admin can end any session
                user.id == sessionUserId || user.role == UserRole.ADMIN
            } ?: false
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
        _canEndSession.value = false
    }
} 