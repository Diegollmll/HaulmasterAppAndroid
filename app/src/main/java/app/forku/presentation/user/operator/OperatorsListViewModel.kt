package app.forku.presentation.user.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.session.VehicleSessionRepository
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
import kotlinx.coroutines.delay

@HiltViewModel
class OperatorsListViewModel @Inject constructor(
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OperatorsListState())
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
                    name = "${it.firstName} ${it.lastName}",
                    image = it.photoUrl,
                    isActive = activeSession,
                    userId = it.id,
                    sessionStartTime = sessionStartTime ?: "",
                    role = it.role
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadOperators(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    _state.value = _state.value.copy(isLoading = true)
                }
                
                // Get current user's business context
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId
                
                if (businessId == null) {
                    android.util.Log.e("OperatorsList", "No business context available")
                    _state.value = _state.value.copy(
                        error = "No business context available",
                        isLoading = false
                    )
                    return@launch
                }
                
                // Get both operators and admins
                val operators = try {
                    userRepository.getUsersByRole(UserRole.OPERATOR)
                } catch (e: Exception) {
                    android.util.Log.e("OperatorsList", "Error getting operators", e)
                    emptyList()
                }
                
                val admins = try {
                    userRepository.getUsersByRole(UserRole.ADMIN)
                } catch (e: Exception) {
                    android.util.Log.e("OperatorsList", "Error getting admins", e)
                    emptyList()
                }
                
                val allUsers = operators + admins
                
                // Get all vehicles and their active sessions with retry
                val vehicles = try {
                    vehicleRepository.getVehicles(businessId)
                } catch (e: Exception) {
                    android.util.Log.e("OperatorsList", "Error getting vehicles", e)
                    emptyList()
                }

                // Get active sessions with retries and rate limiting protection
                val activeSessions = coroutineScope {
                    vehicles.map { vehicle ->
                        async {
                            try {
                                delay(100) // Add small delay between requests to prevent rate limiting
                                vehicleSessionRepository.getActiveSessionForVehicle(vehicle.id, businessId)
                            } catch (e: Exception) {
                                android.util.Log.e("OperatorsList", "Error getting session for vehicle ${vehicle.id}", e)
                                null
                            }
                        }
                    }.mapNotNull { it.await() }
                }

                // Create a map of operator IDs to their active sessions
                val activeOperatorIds = activeSessions.map { it.userId to it.startTime }.toMap()
                
                // Process all users and mark them as active/inactive
                val userInfos = allUsers.mapNotNull { user ->
                    val activeSessionStartTime = activeOperatorIds[user.id]
                    try {
                        getOperatorSessionInfo(
                            userId = user.id,
                            activeSession = activeSessionStartTime != null,
                            sessionStartTime = activeSessionStartTime
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("OperatorsList", "Error getting operator info for ${user.id}", e)
                        null
                    }
                }.sortedWith(
                    compareByDescending<OperatorSessionInfo> { it.isActive }
                    .thenByDescending { it.sessionStartTime }
                )

                _state.value = _state.value.copy(
                    operators = userInfos,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                android.util.Log.e("OperatorsList", "Error in loadOperators", e)
                _state.value = _state.value.copy(
                    error = "Failed to load operators. Please try again.",
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