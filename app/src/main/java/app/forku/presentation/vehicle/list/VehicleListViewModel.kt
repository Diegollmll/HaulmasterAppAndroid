package app.forku.presentation.vehicle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject
import app.forku.presentation.common.utils.parseDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository,
    private val checklistRepository: ChecklistRepository
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleListState())
    val state = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
        loadVehicles()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                // Handle error silently as this is not critical
            }
        }
    }

    fun loadVehicles(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                )

                val currentUser = userRepository.getCurrentUser()
                
                // Get all vehicles based on user role
                val vehicles = when (currentUser?.role) {
                    UserRole.SYSTEM_OWNER -> {
                        // System owner can see all vehicles
                        try {
                            vehicleRepository.getAllVehicles()
                        } catch (e: Exception) {
                            _state.value = _state.value.copy(
                                error = "Error loading vehicles: ${e.message}",
                                isLoading = false,
                                isRefreshing = false
                            )
                            return@launch
                        }
                    }
                    UserRole.ADMIN, UserRole.OPERATOR -> {
                        // ADMIN and OPERATOR can only see vehicles from their business
                        val businessId = currentUser.businessId
                        if (businessId == null) {
                            _state.value = _state.value.copy(
                                error = "No business context available",
                                isLoading = false,
                                isRefreshing = false
                            )
                            return@launch
                        }
                        try {
                            vehicleRepository.getVehicles(businessId)
                        } catch (e: Exception) {
                            _state.value = _state.value.copy(
                                error = "Error loading vehicles: ${e.message}",
                                isLoading = false,
                                isRefreshing = false
                            )
                            return@launch
                        }
                    }
                    null -> {
                        _state.value = _state.value.copy(
                            error = "User not authenticated",
                            isLoading = false,
                            isRefreshing = false
                        )
                        return@launch
                    }
                    else -> {
                        // For any other role, return empty list
                        emptyList()
                    }
                }
                
                // Get all active sessions with rate limiting
                val activeSessions = vehicles.mapNotNull { vehicle ->
                    async {
                        try {
                            // Skip vehicles without a business context
                            if (vehicle.businessId == null) return@async null

                            val session = vehicleSessionRepository.getActiveSessionForVehicle(
                                vehicleId = vehicle.id,
                                businessId = vehicle.businessId
                            )
                            if (session != null) {
                                val operator = try {
                                    // Add delay between requests to avoid rate limiting
                                    kotlinx.coroutines.delay(300)
                                    userRepository.getUserById(session.userId)
                                } catch (e: Exception) {
                                    // If we can't get the operator, continue with null
                                    null
                                }
                                
                                val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${operator?.firstName?.first() ?: "U"}+${operator?.lastName?.first() ?: "U"}&background=random"
                                
                                // Calculate session progress
                                val startTime = parseDateTime(session.startTime)
                                val now = OffsetDateTime.now()
                                val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                                val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
                                
                                vehicle.id to VehicleSessionInfo(
                                    session = session,
                                    sessionStartTime = startTime.format(DateTimeFormatter.ISO_DATE_TIME),
                                    operator = operator,
                                    operatorName = operator?.let { "${it.firstName.first()} ${it.lastName}" } ?: "Unknown",
                                    operatorImage = operator?.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                                    vehicle = vehicle,
                                    vehicleId = vehicle.id,
                                    vehicleType = vehicle.type.name,
                                    progress = progress,
                                    vehicleImage = vehicle.photoModel,
                                    codename = vehicle.codename
                                )
                            } else {
                                // If no active session, get the last completed session
                                val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                                if (lastSession != null) {
                                    val lastOperator = try {
                                        // Add delay between requests to avoid rate limiting
                                        kotlinx.coroutines.delay(300)
                                        userRepository.getUserById(lastSession.userId)
                                    } catch (e: Exception) {
                                        // If we can't get the operator, continue with null
                                        null
                                    }
                                    
                                    val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${lastOperator?.firstName?.first() ?: "U"}+${lastOperator?.lastName?.first() ?: "U"}&background=random"
                                    
                                    vehicle.id to VehicleSessionInfo(
                                        session = lastSession,
                                        sessionStartTime = null, // No active session time
                                        operator = lastOperator,
                                        operatorName = lastOperator?.let { "${it.firstName.first()} ${it.lastName}" } ?: "Unknown",
                                        operatorImage = lastOperator?.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                                        vehicle = vehicle,
                                        vehicleId = vehicle.id,
                                        vehicleType = vehicle.type.name,
                                        progress = null, // No progress for completed session
                                        vehicleImage = vehicle.photoModel,
                                        codename = vehicle.codename
                                    )
                                } else null
                            }
                        } catch (e: Exception) {
                            // If there's an error getting the session, return null
                            null
                        }
                    }
                }.awaitAll().filterNotNull().toMap()

                // Get last preshift checks with rate limiting
                val lastChecks = vehicles.mapNotNull { vehicle ->
                    async {
                        try {
                            // Skip vehicles without a business context
                            if (vehicle.businessId == null) return@async null

                            // Add delay between requests to avoid rate limiting
                            kotlinx.coroutines.delay(300)
                            val lastCheck = checklistRepository.getLastPreShiftCheck(
                                vehicleId = vehicle.id,
                                businessId = vehicle.businessId
                            )
                            vehicle.id to lastCheck
                        } catch (e: Exception) {
                            // If there's an error getting the check, return null
                            vehicle.id to null
                        }
                    }
                }.awaitAll().filterNotNull().toMap()

                _state.value = _state.value.copy(
                    vehicles = vehicles,
                    vehicleSessions = activeSessions,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error loading vehicles. Please try again later.",
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    fun refresh() {
        loadVehicles(showLoading = false)
    }

    fun refreshWithLoading() {
        loadVehicles(showLoading = true)
    }
} 