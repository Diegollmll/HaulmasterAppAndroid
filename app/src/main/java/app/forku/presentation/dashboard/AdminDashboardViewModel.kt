package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import app.forku.core.Constants
import app.forku.data.datastore.AuthDataStore
import app.forku.data.service.GOServicesManager
import app.forku.data.api.VehicleSessionApi
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.usecase.incident.GetUserIncidentCountUseCase
import app.forku.domain.usecase.safetyalert.GetSafetyAlertCountUseCase
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase
import app.forku.core.business.BusinessContextManager

sealed class AuthEvent {
    object NavigateToLogin : AuthEvent()
}

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val incidentRepository: IncidentRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val userRepository: UserRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val authDataStore: AuthDataStore,
    private val goServicesManager: GOServicesManager,
    private val vehicleSessionApi: VehicleSessionApi,
    private val getUserIncidentCountUseCase: GetUserIncidentCountUseCase,
    private val getSafetyAlertCountUseCase: GetSafetyAlertCountUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val businessContextManager: BusinessContextManager
) : ViewModel() {

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _checklistAnswers = MutableStateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>>(emptyMap())
    val checklistAnswers: StateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>> = _checklistAnswers.asStateFlow()

    // Business context from BusinessContextManager
    val businessContextState = businessContextManager.contextState

    init {
        loadCurrentUser()
        // Load business context first, then dashboard data
        viewModelScope.launch {
            businessContextManager.loadBusinessContext()
            loadDashboardData()
            loadOperatingVehiclesCount() // Automatically load operating vehicles count
        }
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
            val currentUser = userRepository.getCurrentUser()
            val businessId = currentUser?.businessId
            
            if (businessId == null) {
                android.util.Log.e("AdminDashboard", "No business context available")
                return null
            }
            
            val vehicle = vehicleRepository.getVehicle(session.vehicleId, businessId)
            android.util.Log.d("AdminDashboard", "Got vehicle: $vehicle")
            
            val operator = userRepository.getUserById(session.userId)
            android.util.Log.d("AdminDashboard", "Got operator: $operator, photoUrl: ${operator?.photoUrl}")

            // Calculate session progress (assuming 8-hour default duration)
            val startTime = parseDateTime(session.startTime)
            val now = OffsetDateTime.now()
            val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
            val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)

            // Remove defaultAvatarUrl and only assign operatorImage if photoUrl is not blank
            VehicleSessionInfo(
                vehicle = vehicle,
                vehicleId = vehicle.id,
                vehicleType = vehicle.type.Name,
                codename = vehicle.codename,
                vehicleImage = vehicle.photoModel,
                session = session,
                operator = operator,
                operatorName = "${operator?.firstName?.firstOrNull() ?: ""}. ${operator?.lastName ?: ""}",
                operatorImage = operator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                sessionStartTime = session.startTime,
                userRole = operator?.role ?: UserRole.OPERATOR,
                progress = progress
            )
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboard", "Error getting session info: ${e.message}")
            null
        }
    }

    private suspend fun getOperatorSessionInfo(session: VehicleSession): OperatorSessionInfo? {
        return try {
            val operator = userRepository.getUserById(session.userId)
            operator?.let {
                OperatorSessionInfo(
                    name = "${it.firstName.firstOrNull() ?: ""}. ${it.lastName}",
                    fullName = it.fullName,
                    username = it.username,
                    image = it.photoUrl?.takeIf { url -> !url.isNullOrBlank() },
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

    private fun loadIncidentCountForDashboard(user: User?) {
        viewModelScope.launch {
            try {
                val count = when (user?.role) {
                    UserRole.ADMIN -> getUserIncidentCountUseCase() // total
                    else -> if (user != null) getUserIncidentCountUseCase(user.id) else 0
                }
                _state.update { it.copy(
                    userIncidentsCount = count,
                    totalIncidentsCount = count
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    userIncidentsCount = 0,
                    totalIncidentsCount = 0
                ) }
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[loadDashboardData] === 🚀 INICIANDO CARGA OPTIMIZADA DE DASHBOARD ===")
            try {
                _state.value = _state.value.copy(isLoading = true)

                val currentUser = userRepository.getCurrentUser()
                val businessId = businessContextManager.getCurrentBusinessId()
                if (currentUser != null) {
                    loadIncidentCountForDashboard(currentUser)
                }

                android.util.Log.d("AdminDashboard", "[loadDashboardData] 🌐 Llamando a getActiveSessionsWithRelatedData con businessId: '$businessId'")
                
                // 🚀 OPTIMIZED: Single API call with all related data
                val dashboardData = vehicleSessionRepository.getActiveSessionsWithRelatedData(businessId  ?: "")
                
                android.util.Log.d("AdminDashboard", "[loadDashboardData] ✅ Datos recibidos - Sessions: ${dashboardData.activeSessions.size}, Vehicles: ${dashboardData.vehicles.size}, Operators: ${dashboardData.operators.size}, ChecklistAnswers: ${dashboardData.checklistAnswers.size}")
                
                // Update checklistAnswers for UI compatibility
                _checklistAnswers.value = dashboardData.checklistAnswers
                
                // Create VehicleSessionInfo from optimized data (no additional API calls needed!)
                val activeSessions = dashboardData.activeSessions.mapNotNull { session ->
                    try {
                        val vehicle = dashboardData.vehicles[session.vehicleId]
                        val operator = dashboardData.operators[session.userId]
                        
                        if (vehicle == null) {
                            android.util.Log.w("AdminDashboard", "[loadDashboardData] ⚠️ Vehicle not found for session ${session.id}")
                            return@mapNotNull null
                        }
                        
                        val operatorFullName = when {
                            !operator?.firstName.isNullOrBlank() || !operator?.lastName.isNullOrBlank() ->
                                listOfNotNull(operator?.firstName, operator?.lastName).joinToString(" ").trim()
                            !operator?.username.isNullOrBlank() -> operator?.username ?: "Sin nombre"
                            else -> "Sin nombre"
                        }
                        
                        val startTime = parseDateTime(session.startTime)
                        val now = OffsetDateTime.now()
                        val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                        val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
                        
                        android.util.Log.d("AdminDashboard", "[loadDashboardData] ✅ Mapped session for vehicle: ${vehicle.codename}, operator: $operatorFullName")
                        
                        VehicleSessionInfo(
                            vehicle = vehicle,
                            vehicleId = vehicle.id,
                            vehicleType = vehicle.type.Name,
                            codename = vehicle.codename,
                            vehicleImage = vehicle.photoModel,
                            session = session,
                            operator = operator,
                            operatorName = operatorFullName,
                            operatorImage = operator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                            sessionStartTime = session.startTime,
                            userRole = operator?.role ?: UserRole.OPERATOR,
                            progress = progress
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboard", "[loadDashboardData] ❌ Error mapping session ${session.id}: ${e.message}", e)
                        null
                    }
                }

                // Get last preshift checks (still needed as separate call)
                val lastChecks = coroutineScope {
                    activeSessions.map { session ->
                        async {
                            try {
                                delay(50) // Reduced delay
                                val lastCheck = checklistRepository.getLastPreShiftCheck(session.vehicle.id, businessId ?: "")
                                session.vehicle.id to lastCheck
                            } catch (e: Exception) {
                                android.util.Log.e("AdminDashboard", "Error getting last check for vehicle ${session.vehicle.id}", e)
                                session.vehicle.id to null
                            }
                        }
                    }.awaitAll().toMap()
                }

                // Create active operators from optimized data (no additional API calls!)
                val activeOperators = activeSessions.mapNotNull { session ->
                    try {
                        session.operator?.let { operator ->
                            val displayName = listOfNotNull(operator.firstName, operator.lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                                .ifBlank { operator.username ?: "Sin nombre" }
                            
                            android.util.Log.d("AdminDashboard", "[loadDashboardData] ✅ Operator from optimized data: $displayName")
                            
                            OperatorSessionInfo(
                                name = displayName,
                                fullName = operator.fullName,
                                username = operator.username,
                                image = operator.photoUrl?.takeIf { !it.isNullOrBlank() },
                                isActive = true,
                                userId = operator.id,
                                sessionStartTime = session.sessionStartTime ?: "",
                                role = operator.role
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboard", "[loadDashboardData] ❌ Error creating operator info: ${e.message}", e)
                        null
                    }
                }

                // Get other dashboard data (incidents, safety alerts)
                val safetyAlertsCount = getSafetyAlertCountUseCase()

                _state.value = _state.value.copy(
                    operatingVehiclesCount = _state.value.operatingVehiclesCount,
                    safetyAlertsCount = safetyAlertsCount,
                    activeVehicleSessions = activeSessions,
                    activeOperators = activeOperators,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    error = null
                )
                
                android.util.Log.d("AdminDashboard", "[loadDashboardData] === 🎉 OPTIMIZED DASHBOARD LOADED ===")
                android.util.Log.d("AdminDashboard", "[loadDashboardData] ✅ Sessions: ${activeSessions.size}, Operators: ${activeOperators.size}")
                android.util.Log.d("AdminDashboard", "[loadDashboardData] 🚀 Performance: 1 API call vs ${activeSessions.size * 3 + 1} traditional calls")
                
                loadOperatingVehiclesCount()
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

    // Refresh con loading para pull-to-refresh o acciones explícitas del usuario
    fun refreshWithLoading() {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[refreshWithLoading] Iniciando refresh con loading...")
            _state.value = _state.value.copy(isLoading = true)
            loadDashboardData()
        }
    }

    // Refresh silencioso (sin loading) cuando volvemos a la pantalla
    fun refresh() {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[refresh] Iniciando refresh...")
            loadDashboardData()
        }
    }

    fun submitFeedback(rating: Int, feedback: String, canContactMe: Boolean) {
        viewModelScope.launch {
            try {
                submitFeedbackUseCase(rating, feedback, canContactMe)
                _state.update { it.copy(feedbackSubmitted = true) }
                delay(3000)
                _state.update { it.copy(feedbackSubmitted = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to submit feedback: ${e.message}") }
            }
        }
    }

    /**
     * Refresh business context and reload dashboard data
     * Useful when user switches business or business assignment changes
     */
    fun refreshBusinessContext() {
        viewModelScope.launch {
            try {
                android.util.Log.d("AdminDashboard", "Refreshing business context...")
                
                // Use BusinessContextManager to refresh context
                businessContextManager.refreshBusinessContext()
                
                // Reload dashboard data with new context
                loadDashboardData()
                
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "Error refreshing business context: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Failed to refresh business context: ${e.message}"
                )
            }
        }
    }

    // Nuevo método para obtener el conteo de vehículos en operación usando business context
    fun loadOperatingVehiclesCount() {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] === INICIANDO CONTEO DE VEHÍCULOS OPERANDO ===")
            try {
                // Check business context first
                val businessContextState = businessContextManager.contextState.value
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] BusinessContextState: hasRealBusinessContext=${businessContextState.hasRealBusinessContext}, businessId=${businessContextState.businessId}, isLoading=${businessContextState.isLoading}")
                
                // Get business ID from BusinessContextManager
                val businessId = businessContextManager.getCurrentBusinessId()
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Business ID obtenido: '$businessId'")
                
                if (businessId == null || businessId == "") {
                    android.util.Log.w("AdminDashboard", "[loadOperatingVehiclesCount] BusinessId está vacío, no se puede obtener conteo")
                    _state.value = _state.value.copy(
                        operatingVehiclesCount = 0,
                        error = "No business context available"
                    )
                    return@launch
                }
                
                // Log current active sessions for comparison
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Sesiones activas actuales en UI: ${_state.value.activeVehicleSessions.size}")
                _state.value.activeVehicleSessions.forEachIndexed { index, session ->
                    android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   Sesión $index: vehicleId=${session.vehicleId}, codename=${session.codename}, sessionId=${session.session.id}")
                }
                
                // Use repository method with business context
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Llamando a vehicleSessionRepository.getOperatingSessionsCount('$businessId')...")
                val count = vehicleSessionRepository.getOperatingSessionsCount(businessId)
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] ✅ Conteo recibido para business '$businessId': $count")
                
                _state.value = _state.value.copy(
                    operatingVehiclesCount = count,
                    isLoading = false,
                    error = null
                )
                
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] === CONTEO COMPLETADO: $count vehículos operando ===")
                
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "[loadOperatingVehiclesCount] ❌ Error: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Error loading operating vehicles count: ${e.message}",
                    operatingVehiclesCount = 0,
                    isLoading = false
                )
            }
        }
    }
} 