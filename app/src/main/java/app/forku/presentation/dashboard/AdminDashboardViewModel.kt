package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.session.SessionRepository
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

data class VehicleSessionInfo(
    val vehicleId: String,
    val vehicleType: String,
    val progress: Float,
    val operatorName: String,
    val operatorImage: String?,
    val sessionStartTime: String,
    val vehicleImage: String?,
    val codename: String
)

data class OperatorSessionInfo(
    val name: String,
    val image: String?,
    val isActive: Boolean,
    val userId: String,
    val sessionStartTime: String
)

data class AdminDashboardState(
    val operatingVehiclesCount: Int = 0,
    val totalIncidentsCount: Int = 0,
    val safetyAlertsCount: Int = 0,
    val activeVehicleSessions: List<VehicleSessionInfo> = emptyList(),
    val activeOperators: List<OperatorSessionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val incidentRepository: IncidentRepository,
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository
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
            android.util.Log.d("AdminDashboard", "Found operator: $operator")
            
            // Calculate session progress (assuming 8-hour shifts)
            val startTime = java.time.OffsetDateTime.parse(session.startTime)
            val now = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
            val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
            val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
            
            // Create session info even if operator is null
            VehicleSessionInfo(
                vehicleId = vehicle.id,
                vehicleType = vehicle.type.displayName,
                progress = progress,
                operatorName = operator?.let { "${it.firstName.first()}. ${it.lastName}" } ?: "Unknown",
                operatorImage = operator?.photoUrl,
                sessionStartTime = session.startTime,
                vehicleImage = vehicle.photoModel,
                codename = vehicle.codename
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
                OperatorSessionInfo(
                    name = "${it.firstName.first()}. ${it.lastName}",
                    image = it.photoUrl,
                    isActive = true, // Since this is from active sessions
                    userId = it.id,
                    sessionStartTime = session.startTime
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

                // Launch parallel coroutines for independent operations
                val vehiclesDeferred = async(Dispatchers.IO) {
                    vehicleRepository.getVehicles()
                }
                val incidentsDeferred = async(Dispatchers.IO) {
                    incidentRepository.getIncidents().getOrNull() ?: emptyList()
                }
                val checksDeferred = async(Dispatchers.IO) {
                    checklistRepository.getAllChecks()
                }

                // Wait for all operations to complete
                val vehicles = vehiclesDeferred.await()
                android.util.Log.d("AdminDashboard", "Fetched vehicles: ${vehicles.map { "${it.id}:${it.status}" }}")
                
                val incidents = incidentsDeferred.await()
                val checks = checksDeferred.await()

                // Process results
                val operatingCount = vehicles.count { it.status == VehicleStatus.IN_USE }
                android.util.Log.d("AdminDashboard", "Operating vehicles count: $operatingCount")

                val incidentsCount = incidents.size
                val safetyAlertsCount = checks.count { check ->
                    check.status == CheckStatus.COMPLETED_PASS.toString() && 
                    check.items.any { item -> 
                        !item.isCritical && item.userAnswer == Answer.FAIL
                    }
                }

                // Get active sessions in parallel
                val activeSessions = coroutineScope {
                    vehicles.map { vehicle ->
                        android.util.Log.d("AdminDashboard", "Checking sessions for vehicle ${vehicle.id} with status ${vehicle.status}")
                        async(Dispatchers.IO) {
                            val session = sessionRepository.getActiveSessionForVehicle(vehicle.id)
                            android.util.Log.d("AdminDashboard", "Vehicle ${vehicle.id} active session: $session")
                            session
                        }
                    }.mapNotNull { it.await() }
                }
                
                android.util.Log.d("AdminDashboard", "Found active sessions: ${activeSessions.map { it.vehicleId }}")

                // Process sessions in parallel
                val (vehicleSessions, activeOperators) = coroutineScope {
                    val vehicleSessionsDeferred = async {
                        val sessions = activeSessions.mapNotNull { session ->
                            val info = getVehicleSessionInfo(session)
                            android.util.Log.d("AdminDashboard", "Processed session ${session.vehicleId} into info: $info")
                            info
                        }.sortedByDescending { it.sessionStartTime }
                        android.util.Log.d("AdminDashboard", "Final vehicle sessions list: ${sessions.map { it.vehicleId }}")
                        sessions
                    }

                    val activeOperatorsDeferred = async {
                        activeSessions.mapNotNull { session ->
                            getOperatorSessionInfo(session)
                        }.distinctBy { it.userId }
                        .sortedByDescending { it.sessionStartTime }
                    }

                    Pair(vehicleSessionsDeferred.await(), activeOperatorsDeferred.await())
                }

                _state.value = _state.value.copy(
                    operatingVehiclesCount = operatingCount,
                    totalIncidentsCount = incidentsCount,
                    safetyAlertsCount = safetyAlertsCount,
                    activeVehicleSessions = vehicleSessions,
                    activeOperators = activeOperators,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "Error loading dashboard data", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error loading dashboard data: ${e.message}"
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
} 