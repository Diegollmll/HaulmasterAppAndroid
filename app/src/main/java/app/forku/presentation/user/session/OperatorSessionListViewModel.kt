package app.forku.presentation.user.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.presentation.dashboard.OperatorSessionInfo
import app.forku.domain.model.user.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@HiltViewModel
class OperatorSessionListViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OperatorSessionListState())
    val state = _state.asStateFlow()

    init {
        loadOperators()
    }

    private suspend fun getOperatorSessionInfo(
        userId: String,
        activeSession: Boolean,
        sessionStartTime: String? = null
    ): OperatorSessionInfo? {
        return try {
            val operator = userRepository.getUserById(userId)
            operator?.let {
                OperatorSessionInfo(
                    name = "${it.firstName.first()}. ${it.lastName}",
                    image = it.photoUrl,
                    isActive = activeSession,
                    userId = it.id,
                    sessionStartTime = sessionStartTime ?: ""
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadOperators(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                )

                // Get all operators
                val operators = userRepository.getUsersByRole(UserRole.OPERATOR)
                
                // Get all vehicles and their active sessions
                val vehicles = vehicleRepository.getVehicles()
                val activeSessions = coroutineScope {
                    vehicles.map { vehicle ->
                        async {
                            sessionRepository.getActiveSessionForVehicle(vehicle.id)
                        }
                    }.mapNotNull { it.await() }
                }

                // Create a map of operator IDs to their active sessions
                val activeOperatorIds = activeSessions.map { it.userId to it.startTime }.toMap()
                
                // Process all operators and mark them as active/inactive
                val operatorInfos = operators.mapNotNull { operator ->
                    val activeSessionStartTime = activeOperatorIds[operator.id]
                    getOperatorSessionInfo(
                        userId = operator.id,
                        activeSession = activeSessionStartTime != null,
                        sessionStartTime = activeSessionStartTime
                    )
                }.sortedWith(
                    compareByDescending<OperatorSessionInfo> { it.isActive }
                    .thenByDescending { it.sessionStartTime }
                )

                _state.value = _state.value.copy(
                    operators = operatorInfos,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    fun refresh() {
        loadOperators(showLoading = false)
    }

    fun refreshWithLoading() {
        loadOperators(showLoading = true)
    }
} 