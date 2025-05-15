package app.forku.presentation.vehicle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.core.Constants
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
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
import app.forku.core.auth.HeaderManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineExceptionHandler

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val headerManager: HeaderManager
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleListState())
    val state = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    private val _checklistAnswers = MutableStateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>>(emptyMap())
    val checklistAnswers = _checklistAnswers.asStateFlow()

    // Hardcoded business ID
    private val hardcodedBusinessId = Constants.BUSINESS_ID

    // Add loading flag to prevent concurrent loads
    private var isLoading = false

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // Loguear si se desea, pero dejar que TokenErrorHandler y BaseScreen manejen la navegación
    }

    sealed class AuthEvent {
        object NavigateToLogin : AuthEvent()
    }

    init {
        loadCurrentUser()
        observeAuthState()
        // Remove automatic loadVehicles() call from init
        // Let the UI trigger it when ready
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

    private fun observeAuthState() {
        viewModelScope.launch {
            headerManager.authState.collect { authState ->
                when (authState) {
                    is HeaderManager.AuthState.NotAuthenticated,
                    is HeaderManager.AuthState.TokenExpired -> {
                        _authEvent.emit(AuthEvent.NavigateToLogin)
                    }
                    else -> { /* No action needed */ }
                }
            }
        }
    }

    fun loadVehicles(showLoading: Boolean = true) {
        if (isLoading) return  // Skip if already loading
        
        viewModelScope.launch(exceptionHandler) {
            try {
                isLoading = true
                _state.value = _state.value.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                )

                // Use hardcoded business ID instead of checking user role
                val vehicles = try {
                    vehicleRepository.getVehicles(hardcodedBusinessId)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 401 || e.code() == 403) {
                        throw e // Let auth errors bubble up for global handling
                    }
                    _state.value = _state.value.copy(
                        error = "Error loading vehicles: ${e.message()}",
                        isLoading = false,
                        isRefreshing = false
                    )
                    isLoading = false
                    return@launch
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        error = "Error loading vehicles: ${e.message}",
                        isLoading = false,
                        isRefreshing = false
                    )
                    isLoading = false
                    return@launch
                }
                
                // Get all active sessions with rate limiting
                val activeSessions = vehicles.mapNotNull { vehicle ->
                    async {
                        try {
                            val session = vehicleSessionRepository.getActiveSessionForVehicle(
                                vehicleId = vehicle.id,
                                businessId = hardcodedBusinessId
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
                                
                                val operatorName = when {
                                    !operator?.firstName.isNullOrBlank() || !operator?.lastName.isNullOrBlank() ->
                                        listOfNotNull(operator?.firstName, operator?.lastName).joinToString(" ").trim()
                                    !operator?.username.isNullOrBlank() -> operator?.username ?: "Sin nombre"
                                    else -> "Sin nombre"
                                }
                                val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${operator?.firstName?.firstOrNull() ?: "U"}+${operator?.lastName?.firstOrNull() ?: "U"}&background=random"
                                
                                // Calculate session progress
                                val startTime = parseDateTime(session.startTime)
                                val now = OffsetDateTime.now()
                                val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                                val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
                                
                                vehicle.id to VehicleSessionInfo(
                                    session = session,
                                    sessionStartTime = startTime.format(DateTimeFormatter.ISO_DATE_TIME),
                                    operator = operator,
                                    operatorName = operatorName,
                                    operatorImage = operator?.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                                    vehicle = vehicle,
                                    vehicleId = vehicle.id,
                                    vehicleType = vehicle.type.Name,
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
                                    
                                    val lastOperatorName = when {
                                        !lastOperator?.firstName.isNullOrBlank() || !lastOperator?.lastName.isNullOrBlank() ->
                                            listOfNotNull(lastOperator?.firstName, lastOperator?.lastName).joinToString(" ").trim()
                                        !lastOperator?.username.isNullOrBlank() -> lastOperator?.username ?: "Sin nombre"
                                        else -> "Sin nombre"
                                    }
                                    val defaultAvatarUrl = "https://ui-avatars.com/api/?name=${lastOperator?.firstName?.firstOrNull() ?: "U"}+${lastOperator?.lastName?.firstOrNull() ?: "U"}&background=random"
                                    
                                    vehicle.id to VehicleSessionInfo(
                                        session = lastSession,
                                        sessionStartTime = null, // No active session time
                                        operator = lastOperator,
                                        operatorName = lastOperatorName,
                                        operatorImage = lastOperator?.photoUrl?.takeIf { url -> url.isNotEmpty() } ?: defaultAvatarUrl,
                                        vehicle = vehicle,
                                        vehicleId = vehicle.id,
                                        vehicleType = vehicle.type.Name,
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
                            // Add delay between requests to avoid rate limiting
                            kotlinx.coroutines.delay(300)
                            val lastCheck = checklistRepository.getLastPreShiftCheck(
                                vehicleId = vehicle.id,
                                businessId = hardcodedBusinessId
                            )
                            vehicle.id to lastCheck
                        } catch (e: Exception) {
                            // If there's an error getting the check, return null
                            vehicle.id to null
                        }
                    }
                }.awaitAll().filterNotNull().toMap()

                // Fetch last ChecklistAnswer for each vehicle (if available)
                val checklistAnswers = vehicles.mapNotNull { vehicle ->
                    async {
                        try {
                            // 1. Buscar sesión activa
                            val session = vehicleSessionRepository.getActiveSessionForVehicle(vehicle.id, hardcodedBusinessId)
                            val checklistAnswerId = session?.checkId
                            // 2. Si no hay sesión activa, buscar la última sesión finalizada
                            val answer = if (!checklistAnswerId.isNullOrBlank()) {
                                checklistAnswerRepository.getById(checklistAnswerId)
                            } else {
                                val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                                lastSession?.checkId?.let { checklistAnswerRepository.getById(it) }
                            }
                            if (answer != null) vehicle.id to answer else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull().toMap()
                _checklistAnswers.value = checklistAnswers

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
            } finally {
                isLoading = false
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