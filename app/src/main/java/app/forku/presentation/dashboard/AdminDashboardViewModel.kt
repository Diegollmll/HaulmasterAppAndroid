package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.User
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import app.forku.presentation.common.utils.parseDateTime
import kotlinx.coroutines.flow.update
import java.time.OffsetDateTime



@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val incidentRepository: IncidentRepository,
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val vehicleSessionRepository: VehicleSessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
        loadDashboardData()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error loading user: ${e.message}"
                )
            }
        }
    }

    private suspend fun getVehicleSessionInfo(session: VehicleSession): VehicleSessionInfo? {
        return try {
            android.util.Log.d("AdminDashboard", "Getting info for session: $session")
            val vehicle = vehicleRepository.getVehicle(session.vehicleId)
            android.util.Log.d("AdminDashboard", "Found vehicle: $vehicle")
            val operator = userRepository.getUserById(session.userId)
            android.util.Log.d("AdminDashboard", "Found operator: $operator with photoUrl: ${operator?.photoUrl}")
            
            // Calculate session progress (assuming 8-hour shifts)
            val startTime = parseDateTime(session.startTime)
            val now = OffsetDateTime.now()
            val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()

            val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)

            // Default avatar URL for when photoUrl is empty
            val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${operator?.firstName?.first() ?: "U"}+${operator?.lastName?.first() ?: "U"}&background=random"
            
            // Create session info even if operator is null
            VehicleSessionInfo(
                vehicle = vehicle,
                vehicleId = vehicle.id,
                vehicleType = vehicle.type.displayName,
                progress = progress,
                operatorName = operator?.let { "${it.firstName.first()}. ${it.lastName}" } ?: "Unknown",
                operatorImage = operator?.photoUrl?.takeIf { it.isNotEmpty() } ?: defaultAvatarUrl,
                sessionStartTime = session.startTime,
                vehicleImage = vehicle.photoModel,
                codename = vehicle.codename,
                session = session,
                operator = operator
            )
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboard", "Error getting vehicle session info", e)
            null
        }
    }

    private suspend fun getOperatorSessionInfo(session: VehicleSession): OperatorSessionInfo? {
        return try {
            val operator = userRepository.getUserById(session.userId)
            operator?.let {
                // Default avatar URL for when photoUrl is empty
                val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${it.firstName.first()}+${it.lastName.first()}&background=random"
                
                OperatorSessionInfo(
                    name = "${it.firstName.first()}. ${it.lastName}",
                    image = it.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                    isActive = true, // This operator has an active session since we're getting info from a session
                    userId = it.id,
                    sessionStartTime = session.startTime,
                    role = it.role
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                // Get all vehicles with error handling
                val vehicles = try {
                    vehicleRepository.getVehicles()
                } catch (e: Exception) {
                    android.util.Log.e("AdminDashboard", "Error getting vehicles", e)
                    emptyList()
                }
                
                // Get active sessions with rate limiting protection
                val activeSessions = coroutineScope {
                    vehicles.map { vehicle ->
                        async {
                            try {
                                delay(100) // Add small delay between requests to prevent rate limiting
                                val session = vehicleSessionRepository.getActiveSessionForVehicle(vehicle.id)
                                session?.let { 
                                    val operator = userRepository.getUserById(it.userId)
                                    val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${operator?.firstName?.first() ?: "U"}+${operator?.lastName?.first() ?: "U"}&background=random"
                                    
                                    // Calculate session progress
                                    val startTime = parseDateTime(it.startTime)
                                    val now = OffsetDateTime.now()
                                    val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                                    val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
                                    
                                    VehicleSessionInfo(
                                        session = it,
                                        sessionStartTime = it.startTime,
                                        operator = operator,
                                        operatorName = operator?.let { user -> "${user.firstName.first()}. ${user.lastName}" } ?: "Unknown",
                                        operatorImage = operator?.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                                        vehicle = vehicle,
                                        vehicleId = vehicle.id,
                                        vehicleType = vehicle.type.displayName,
                                        progress = progress,
                                        vehicleImage = vehicle.photoModel,
                                        codename = vehicle.codename
                                    )
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("AdminDashboard", "Error getting session info for vehicle ${vehicle.id}", e)
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()
                }

                // Get last preshift checks with error handling
                val lastChecks = coroutineScope {
                    activeSessions.map { session ->
                        async {
                            try {
                                delay(100) // Add small delay between requests
                                val lastCheck = checklistRepository.getLastPreShiftCheck(session.vehicle.id)
                                session.vehicle.id to lastCheck
                            } catch (e: Exception) {
                                android.util.Log.e("AdminDashboard", "Error getting last check for vehicle ${session.vehicle.id}", e)
                                session.vehicle.id to null
                            }
                        }
                    }.awaitAll().toMap()
                }

                // Get active operators with error handling
                val activeOperators = activeSessions.mapNotNull { session ->
                    try {
                        session.operator?.let { operator ->
                            val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${operator.firstName.first()}+${operator.lastName.first()}&background=random"
                            OperatorSessionInfo(
                                name = "${operator.firstName.first()}. ${operator.lastName}",
                                image = operator.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                                isActive = true, // They have an active session
                                userId = operator.id,
                                sessionStartTime = session.sessionStartTime ?: "",
                                role = operator.role
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboard", "Error creating operator info for session ${session.session.id}", e)
                        null
                    }
                }

                // Get total incidents with error handling
                val incidents = try {
                    incidentRepository.getIncidents().getOrDefault(emptyList())
                } catch (e: Exception) {
                    android.util.Log.e("AdminDashboard", "Error getting incidents", e)
                    emptyList()
                }

                // Get all checks with error handling
                val allChecks = try {
                    checklistRepository.getAllChecks()
                } catch (e: Exception) {
                    android.util.Log.e("AdminDashboard", "Error getting all checks", e)
                    emptyList()
                }

                val safetyAlertsCount = allChecks.flatMap { check -> 
                    check.items.filter { item -> 
                        !item.isCritical && item.userAnswer == Answer.FAIL
                    }
                }.size

                _state.value = _state.value.copy(
                    operatingVehiclesCount = activeSessions.size,
                    totalIncidentsCount = incidents.size,
                    safetyAlertsCount = safetyAlertsCount,
                    activeVehicleSessions = activeSessions,
                    activeOperators = activeOperators,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "Error in loadDashboardData", e)
                _state.value = _state.value.copy(
                    error = "Failed to load dashboard data. Please try again.",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // Refresh silencioso (sin loading) cuando volvemos a la pantalla
    fun refresh() {
        viewModelScope.launch {
            loadDashboardData()
        }
    }

    // Refresh con loading para pull-to-refresh o acciones explícitas del usuario
    fun refreshWithLoading() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            loadDashboardData()
        }
    }

    fun submitFeedback(rating: Int, feedback: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement API call to submit feedback
                // For now, just log it
                android.util.Log.d("Feedback", "Submitting feedback - Rating: $rating, Feedback: $feedback")
                android.util.Log.d("Feedback", "User: ${currentUser.value?.id}")
                
                // You could show a success message in the UI
                _state.update { it.copy(
                    feedbackSubmitted = true
                )}
                
                // Reset the feedback submitted state after a delay
                delay(3000)
                _state.update { it.copy(
                    feedbackSubmitted = false
                )}
            } catch (e: Exception) {
                android.util.Log.e("Feedback", "Error submitting feedback", e)
                _state.update { it.copy(
                    error = "Failed to submit feedback: ${e.message}"
                )}
            }
        }
    }
} 