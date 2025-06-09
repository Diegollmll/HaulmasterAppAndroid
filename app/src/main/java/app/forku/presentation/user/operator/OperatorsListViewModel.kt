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
import app.forku.core.Constants

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

    private fun createOperatorSessionInfo(
        user: app.forku.domain.model.user.User,
        activeSession: Boolean,
        sessionStartTime: String? = null
    ): OperatorSessionInfo {
        return OperatorSessionInfo(
            name = "${user.firstName} ${user.lastName}",
            fullName = user.fullName,
            username = user.username,
            image = user.photoUrl,
            isActive = activeSession,
            userId = user.id,
            sessionStartTime = sessionStartTime ?: "",
            role = user.role
        )
    }

    fun loadOperators(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    _state.value = _state.value.copy(isLoading = true)
                }
                
                // Get current user's business context
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId ?: Constants.BUSINESS_ID
                
                // Get all users with included role data in one optimized call
                val allUsers = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    android.util.Log.e("OperatorsList", "Error getting users", e)
                    emptyList()
                }
                
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
                
                // Process all users and mark them as active/inactive using optimized data
                val userInfos = allUsers.map { user ->
                    val activeSessionStartTime = activeOperatorIds[user.id]
                    android.util.Log.d("OperatorsList", "=== Processing User ===")
                    android.util.Log.d("OperatorsList", "User ID: ${user.id}")
                    android.util.Log.d("OperatorsList", "User fullName: ${user.fullName}")
                    android.util.Log.d("OperatorsList", "User email: ${user.email}")
                    android.util.Log.d("OperatorsList", "User username: ${user.username}")
                    android.util.Log.d("OperatorsList", "User role: ${user.role}")
                    android.util.Log.d("OperatorsList", "Active session: ${activeSessionStartTime != null}")
                    
                    val operatorInfo = createOperatorSessionInfo(
                        user = user,
                        activeSession = activeSessionStartTime != null,
                        sessionStartTime = activeSessionStartTime
                    )
                    
                    android.util.Log.d("OperatorsList", "Created OperatorSessionInfo with role: ${operatorInfo.role}")
                    operatorInfo
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