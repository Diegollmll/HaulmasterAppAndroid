package app.forku.presentation.vehicle.list

import android.util.Log
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
import kotlinx.coroutines.coroutineScope

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
        if (isLoading) return
        
        viewModelScope.launch(exceptionHandler) {
            try {
                isLoading = true
                _state.value = _state.value.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                )

                // 1. Load vehicles first
                val vehicles = try {
                    vehicleRepository.getVehicles(hardcodedBusinessId)
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 401 || e.code() == 403) {
                        throw e
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

                // 2. Process all data in parallel without artificial delays
                val (activeSessions, lastChecks, checklistAnswers) = coroutineScope {
                    val activeSessionsDeferred = async {
                        vehicles.mapNotNull { vehicle ->
                            try {
                                val session = vehicleSessionRepository.getActiveSessionForVehicle(
                                    vehicleId = vehicle.id,
                                    businessId = hardcodedBusinessId
                                )
                                
                                if (session != null) {
                                    val operator = userRepository.getUserById(session.userId)
                                    val operatorName = when {
                                        !operator?.firstName.isNullOrBlank() || !operator?.lastName.isNullOrBlank() ->
                                            listOfNotNull(operator?.firstName, operator?.lastName).joinToString(" ").trim()
                                        !operator?.username.isNullOrBlank() -> operator?.username ?: "Sin nombre"
                                        else -> "Sin nombre"
                                    }
                                    
                                    val startTime = parseDateTime(session.startTime)
                                    val now = OffsetDateTime.now()
                                    val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
                                    val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)

                                    Log.d("VehicleItem", "operator photoUrl value in VehicleListViewModel: ${operator?.photoUrl}")

                                    vehicle.id to VehicleSessionInfo(
                                        session = session,
                                        sessionStartTime = startTime.format(DateTimeFormatter.ISO_DATE_TIME),
                                        operator = operator,
                                        operatorName = operatorName,
                                        operatorImage = operator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                                        vehicle = vehicle,
                                        vehicleId = vehicle.id,
                                        vehicleType = vehicle.type.Name,
                                        progress = progress,
                                        vehicleImage = vehicle.photoModel,
                                        codename = vehicle.codename
                                    )
                                } else {
                                    val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                                    if (lastSession != null) {
                                        val lastOperator = userRepository.getUserById(lastSession.userId)
                                        val lastOperatorName = when {
                                            !lastOperator?.firstName.isNullOrBlank() || !lastOperator?.lastName.isNullOrBlank() ->
                                                listOfNotNull(lastOperator?.firstName, lastOperator?.lastName).joinToString(" ").trim()
                                            !lastOperator?.username.isNullOrBlank() -> lastOperator?.username ?: "Sin nombre"
                                            else -> "Sin nombre"
                                        }
                                        
                                        vehicle.id to VehicleSessionInfo(
                                            session = lastSession,
                                            sessionStartTime = null,
                                            operator = lastOperator,
                                            operatorName = lastOperatorName,
                                            operatorImage = lastOperator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                                            vehicle = vehicle,
                                            vehicleId = vehicle.id,
                                            vehicleType = vehicle.type.Name,
                                            progress = null,
                                            vehicleImage = vehicle.photoModel,
                                            codename = vehicle.codename
                                        )
                                    } else null
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }.toMap()
                    }
                    val lastChecksDeferred = async {
                        vehicles.mapNotNull { vehicle ->
                            try {
                                vehicle.id to checklistRepository.getLastPreShiftCheck(
                                    vehicleId = vehicle.id,
                                    businessId = hardcodedBusinessId
                                )
                            } catch (e: Exception) {
                                vehicle.id to null
                            }
                        }.toMap()
                    }
                    val checklistAnswersDeferred = async {
                        vehicles.mapNotNull { vehicle ->
                            try {
                                val session = vehicleSessionRepository.getActiveSessionForVehicle(vehicle.id, hardcodedBusinessId)
                                val checklistAnswerId = session?.checkId
                                
                                val answer = if (!checklistAnswerId.isNullOrBlank()) {
                                    checklistAnswerRepository.getById(checklistAnswerId)
                                } else {
                                    val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                                    lastSession?.checkId?.let { checklistAnswerRepository.getById(it) }
                                } ?: checklistAnswerRepository.getLastChecklistAnswerForVehicle(vehicle.id)
                                
                                if (answer != null) vehicle.id to answer else null
                            } catch (e: Exception) {
                                null
                            }
                        }.toMap()
                    }
                    Triple(
                        activeSessionsDeferred.await(),
                        lastChecksDeferred.await(),
                        checklistAnswersDeferred.await()
                    )
                }

                // 3. Update state with all data
                _state.value = _state.value.copy(
                    vehicles = vehicles,
                    vehicleSessions = activeSessions,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
                _checklistAnswers.value = checklistAnswers

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