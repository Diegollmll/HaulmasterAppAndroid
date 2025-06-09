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

                // 🚀 OPTIMIZED: Single API call with all related data
                val vehiclesWithData = try {
                    vehicleRepository.getVehiclesWithRelatedData(hardcodedBusinessId)
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

                // Transform the optimized data to the existing state structure
                val vehicles = vehiclesWithData.map { it.vehicle }
                val activeSessions = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.activeSessions.firstOrNull { 
                        it.sessionStartTime != null 
                    }
                }.filterValues { it != null }.mapValues { it.value!! }
                
                val lastChecks = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.lastPreShiftCheck?.let { checklistAnswer ->
                        // Convert ChecklistAnswer to PreShiftCheck for compatibility
                        app.forku.domain.model.checklist.PreShiftCheck(
                            id = checklistAnswer.id,
                            vehicleId = checklistAnswer.vehicleId ?: vehicleData.vehicle.id,
                            userId = checklistAnswer.goUserId,
                            status = checklistAnswer.status.toString(),
                            startDateTime = checklistAnswer.startDateTime,
                            endDateTime = checklistAnswer.endDateTime,
                            items = emptyList() // Will be populated if needed
                        )
                    }
                }
                
                val checklistAnswers = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.checklistAnswers
                        .sortedByDescending { 
                            // Use same logic as repository: lastCheckDateTime or endDateTime as fallback
                            it.lastCheckDateTime.takeIf { it.isNotBlank() } ?: it.endDateTime 
                        }
                        .firstOrNull()
                }.filterValues { it != null }.mapValues { it.value!! }

                // Update state with all data (same structure as before, but from optimized source)
                _state.value = _state.value.copy(
                    vehicles = vehicles,
                    vehicleSessions = activeSessions,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
                _checklistAnswers.value = checklistAnswers

                // The actual number of API calls is now handled in the repository
                // 1 vehicle call + N unique user calls (instead of 1 + vehicles * 5)
                Log.d("VehicleListViewModel", "✅ OPTIMIZED: Loaded ${vehicles.size} vehicles with optimized API calls")

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