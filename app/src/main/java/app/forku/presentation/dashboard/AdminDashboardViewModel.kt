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
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _checklistAnswers = MutableStateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>>(emptyMap())
    val checklistAnswers: StateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>> = _checklistAnswers.asStateFlow()

    init {
        loadCurrentUser()
        loadDashboardData()
        loadOperatingVehiclesCount() // Automatically load operating vehicles count
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
            android.util.Log.d("AdminDashboard", "[loadDashboardData] Iniciando carga de dashboard data...")
            try {
                _state.value = _state.value.copy(isLoading = true)

                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId ?: Constants.BUSINESS_ID
                if (currentUser != null) {
                    loadIncidentCountForDashboard(currentUser)
                }

                // Obtener sesiones activas directamente de la API
                val sessionResponse = vehicleSessionApi.getAllSessions(businessId)
                val sessionDtos = if (sessionResponse.isSuccessful) sessionResponse.body() ?: emptyList() else emptyList()
                val activeSessionDtos = sessionDtos.filter { it.Status == 0 && it.EndTime == null }

                // Obtener ChecklistAnswers para cada sesión activa
                val checklistAnswers = coroutineScope {
                    activeSessionDtos.map { dto ->
                        async {
                            val answer = try { checklistAnswerRepository.getById(dto.ChecklistAnswerId) } catch (e: Exception) { null }
                            dto.ChecklistAnswerId to answer
                        }
                    }.awaitAll().filter { it.second != null }.associate { it.first to it.second!! }
                }
                _checklistAnswers.value = checklistAnswers

                // Mapear a VehicleSessionInfo
                val activeSessions = coroutineScope {
                    activeSessionDtos.map { dto ->
                        async {
                            try {
                                val vehicle = vehicleRepository.getVehicle(dto.VehicleId, businessId)
                                val operator = userRepository.getUserById(dto.GOUserId)
                                val operatorFullName = when {
                                    !operator?.firstName.isNullOrBlank() || !operator?.lastName.isNullOrBlank() ->
                                        listOfNotNull(operator?.firstName, operator?.lastName).joinToString(" ").trim()
                                    !operator?.username.isNullOrBlank() -> operator?.username ?: "Sin nombre"
                                    else -> "Sin nombre"
                                }
                                val username = operator?.username
                                val initials = when {
                                    !operator?.firstName.isNullOrBlank() -> operator?.firstName?.first().toString()
                                    !username.isNullOrBlank() -> username.first().toString()
                                    else -> "?"
                                } + when {
                                    !operator?.lastName.isNullOrBlank() -> operator?.lastName?.first().toString()
                                    !username.isNullOrBlank() && username.length > 1 -> username[1].toString()
                                    else -> "?"
                                }
                                val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${initials}&background=random"
                                val startTime = parseDateTime(dto.StartTime)
                                val now = OffsetDateTime.now()
                                val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                                android.util.Log.d(
                                    "AdminDashboardSessionTimer",
                                    """
                                    [SESSION TIMER DEBUG]
                                    - Vehicle: ${vehicle.codename}
                                    - SessionId: ${dto.Id}
                                    - Raw StartTime (DTO): ${dto.StartTime}
                                    - Parsed StartTime: $startTime
                                    - Now: $now
                                    - Elapsed Minutes: $elapsedMinutes
                                    """.trimIndent()
                                )
                                val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
                                VehicleSessionInfo(
                                    vehicle = vehicle,
                                    vehicleId = vehicle.id,
                                    vehicleType = vehicle.type.Name,
                                    codename = vehicle.codename,
                                    vehicleImage = vehicle.photoModel,
                                    session = VehicleSession(
                                        id = dto.Id,
                                        vehicleId = dto.VehicleId,
                                        userId = dto.GOUserId,
                                        checkId = dto.ChecklistAnswerId,
                                        startTime = dto.StartTime,
                                        endTime = dto.EndTime,
                                        status = if (dto.Status == 0) VehicleSessionStatus.OPERATING else VehicleSessionStatus.NOT_OPERATING,
                                        startLocationCoordinates = dto.StartLocationCoordinates,
                                        endLocationCoordinates = dto.EndLocationCoordinates,
                                        durationMinutes = null, // No hay campo directo en el DTO
                                        timestamp = dto.Timestamp,
                                        closeMethod = null, // Mapear si es necesario
                                        closedBy = dto.ClosedBy,
                                        notes = null
                                    ),
                                    operator = operator,
                                    operatorName = operatorFullName,
                                    operatorImage = operator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                                    sessionStartTime = dto.StartTime,
                                    userRole = operator?.role ?: UserRole.OPERATOR,
                                    progress = progress
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("AdminDashboard", "Error mapping VehicleSessionInfo", e)
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
                                val lastCheck = checklistRepository.getLastPreShiftCheck(session.vehicle.id, businessId)
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
                        if (session.operator == null) {
                            android.util.Log.e("AdminDashboard", "[activeOperators] session.operator is null for sessionId=${session.session.id}, userId=${session.session.userId}")
                        } else {
                            android.util.Log.d("AdminDashboard", "[activeOperators] session.operator: id=${session.operator.id}, firstName='${session.operator.firstName}', lastName='${session.operator.lastName}', username='${session.operator.username}', photoUrl='${session.operator.photoUrl}'")
                        }
                        session.operator?.let { operator ->
                            val displayName = listOfNotNull(operator.firstName, operator.lastName)
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                                .ifBlank { operator.username ?: "Sin nombre" }
                            android.util.Log.d("AdminDashboard", "[activeOperators] Adding OperatorSessionInfo: name='$displayName', userId=${operator.id}, sessionStartTime=${session.sessionStartTime}")
                            OperatorSessionInfo(
                                name = displayName,
                                fullName = operator.fullName,
                                username = operator.username,
                                image = operator.photoUrl?.takeIf { !it.isNullOrBlank() },
                                isActive = true, // They have an active session
                                userId = operator.id,
                                sessionStartTime = session.sessionStartTime ?: "",
                                role = operator.role
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboard", "[activeOperators] Error creating operator info for session ${session.session.id}", e)
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
                android.util.Log.d("AdminDashboard", "[loadDashboardData] Estado actualizado, activeVehicleSessions: ${activeSessions.size}")
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

    // Nuevo método para obtener el conteo de vehículos en operación usando el endpoint optimizado
    fun loadOperatingVehiclesCount() {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Llamando a la API de conteo de vehículos operando...")
            try {
                var csrfToken = authDataStore.getCsrfToken()
                if (csrfToken == null) {
                    android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] CSRF token is null, forcing refresh from backend...")
                    goServicesManager.getCsrfToken(forceRefresh = true)
                    csrfToken = authDataStore.getCsrfToken()
                }
                val cookie = authDataStore.getAntiforgeryCookieSuspend()
                val response = vehicleSessionApi.getOperatingSessionsCount(
                    filter = "Status == 0",
                    csrfToken = csrfToken ?: "",
                    cookie = cookie ?: ""
                )
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Respuesta de la API: isSuccessful=${response.isSuccessful}, body=${response.body()}")
                if (response.isSuccessful) {
                    val count = response.body() ?: 0
                    android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Conteo recibido de API: $count")
                    _state.value = _state.value.copy(
                        operatingVehiclesCount = count,
                        isLoading = false,
                        error = null
                    )
                } else {
                    android.util.Log.e("AdminDashboard", "[loadOperatingVehiclesCount] Error en respuesta: ${response.code()}")
                    _state.value = _state.value.copy(
                        error = "Error loading operating vehicles count: ${response.code()}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "[loadOperatingVehiclesCount] Error general: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Error loading operating vehicles count: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
} 