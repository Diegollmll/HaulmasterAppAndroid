package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckCurrentUserUseCase
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.model.user.User
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckByVehicleUseCase
import app.forku.presentation.user.login.LoginState
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.delay

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getLastPreShiftCheckCurrentUserUseCase: GetLastPreShiftCheckCurrentUserUseCase,
    private val getLastPreShiftCheckUseCase: GetLastPreShiftCheckByVehicleUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _tourCompleted = MutableStateFlow(false)
    val tourCompleted: StateFlow<Boolean> = _tourCompleted.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _hasToken = MutableStateFlow(false)
    val hasToken: StateFlow<Boolean> = _hasToken.asStateFlow()

    // Add mutex to prevent concurrent loadDashboard calls
    private val loadDashboardMutex = Mutex()
    
    init {
        loadCurrentUser()
        loadTourCompletionStatus()
        checkLoginState()
        checkAuthToken()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error loading user: ${e.message}") }
            }
        }
    }
    
    private fun loadTourCompletionStatus() {
        viewModelScope.launch {
            val completed = userRepository.getTourCompletionStatus()
            _tourCompleted.value = completed
        }
    }
    
    private fun checkLoginState() {
        viewModelScope.launch {
            val currentUser = getCurrentUser()
            _loginState.value = if (currentUser != null) {
                LoginState.Success(user = currentUser)
            } else {
                LoginState.Initial
            }
        }
    }
    
    private fun checkAuthToken() {
        viewModelScope.launch {
            try {
                val token = userRepository.getAuthToken()
                _hasToken.value = !token.isNullOrEmpty()
            } catch (e: Exception) {
                _hasToken.value = false
            }
        }
    }
    
    private suspend fun loadDashboard(showLoading: Boolean = false) {
        // Use mutex to prevent concurrent loadDashboard calls
        if (!loadDashboardMutex.tryLock()) {
            android.util.Log.d("DashboardViewModel", "Skipping loadDashboard - already in progress")
            return
        }
        
        try {
            if (showLoading) {
                android.util.Log.d("DashboardViewModel", "Setting loading state to true")
                _state.update { it.copy(isLoading = true) }
            }
            
            val currentUser = userRepository.getCurrentUser()
                ?: throw Exception("User not authenticated")

            // Load all vehicles
            val vehicles = vehicleRepository.getVehicles()
            
            // Load only active sessions (where endTime is null)
            val activeSessions = vehicleSessionRepository.getSessions()
                .filter { session -> 
                    session.endTime == null && 
                    // Ensure the vehicle exists for this session
                    vehicles.any { vehicle -> vehicle.id == session.vehicleId }
                }
                .distinctBy { it.vehicleId } // Ensure only one active session per vehicle
            
            // Load all users involved in active sessions
            val userIds = activeSessions.map { it.userId }.distinct()
            val users = userIds.mapNotNull { userId ->
                userRepository.getUserById(userId)
            }
            
            // Load latest checks for each vehicle
            val checks = vehicles.mapNotNull { vehicle ->
                getLastPreShiftCheckUseCase(vehicle.id)
            }

            android.util.Log.d("DashboardViewModel", "Getting current session")
            val currentSession = vehicleSessionRepository.getCurrentSession()

            // Get last session for current user
            val lastSession = try {
                if (currentSession == null) {
                    vehicleSessionRepository.getOperatorSessionHistory()
                        .filter { it.userId == currentUser.id }
                        .maxByOrNull { it.startTime }
                } else null
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Error getting last session", e)
                null
            }

            android.util.Log.d("DashboardViewModel", "Getting session vehicle")
            val sessionVehicle = (currentSession ?: lastSession)?.let { 
                getVehicleUseCase(it.vehicleId)
            }
            
            android.util.Log.d("DashboardViewModel", "Getting last pre-shift check")
            val lastPreShiftCheck = try {
                getLastPreShiftCheckCurrentUserUseCase()
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Error getting last pre-shift check", e)
                null
            }
            
            android.util.Log.d("DashboardViewModel", "Getting check vehicle")
            val checkVehicle = if (currentSession == null) {
                lastPreShiftCheck?.let { 
                    getVehicleUseCase(it.vehicleId)
                }
            } else null

            android.util.Log.d("DashboardViewModel", "Updating state with loaded data")
            _state.update {
                it.copy(
                    currentSession = currentSession,
                    lastSession = lastSession,
                    displayVehicle = sessionVehicle ?: checkVehicle,
                    lastPreShiftCheck = lastPreShiftCheck,
                    isLoading = false,
                    error = null,
                    vehicles = vehicles,
                    activeSessions = activeSessions,
                    users = users,
                    checks = checks
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardViewModel", "Error in loadDashboard", e)
            _state.update {
                it.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        } finally {
            loadDashboardMutex.unlock()
        }
    }

    // Refresh silencioso (sin loading) cuando volvemos a la pantalla
    fun refresh() {
        android.util.Log.d("DashboardViewModel", "Silent refresh called")
        viewModelScope.launch {
            loadDashboard(showLoading = false)
        }
    }

    // Refresh con loading para pull-to-refresh o acciones explícitas del usuario
    fun refreshWithLoading() {
        android.util.Log.d("DashboardViewModel", "refreshWithLoading called")
        viewModelScope.launch {
            android.util.Log.d("DashboardViewModel", "Starting refresh with loading")
            loadDashboard(showLoading = true)
            android.util.Log.d("DashboardViewModel", "Refresh with loading completed")
        }
    }

    fun endCurrentSession() {
        viewModelScope.launch {
            try {
                android.util.Log.d("appflow DashboardViewModel", "Starting endCurrentSession")
                _state.update { it.copy(isLoading = true) }
                
                val currentSession = state.value.currentSession
                android.util.Log.d("appflow DashboardViewModel", "Current session: $currentSession")
                
                if (currentSession != null) {
                    android.util.Log.d("appflow DashboardViewModel", "Ending session with ID: ${currentSession.id}")
                    vehicleSessionRepository.endSession(currentSession.id)
                    
                    // Actualizar el estado después de finalizar la sesión
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            currentSession = null,
                            displayVehicle = null,
                            error = null,
                            // Clear active sessions for this user
                            activeSessions = it.activeSessions.filter { session -> 
                                session.id != currentSession.id 
                            }
                        )
                    }
                    
                    android.util.Log.d("appflow DashboardViewModel", "Session ended successfully, reloading dashboard")
                    // Recargar el dashboard para obtener el nuevo estado
                    loadDashboard(showLoading = false)
                }
            } catch (e: Exception) {
                android.util.Log.e("appflow DashboardViewModel", "Error ending session", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al finalizar sesión: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun getCurrentUser(): User? = _currentUser.value

    fun setTourCompleted() {
        viewModelScope.launch {
            userRepository.setTourCompleted()
            _tourCompleted.value = true
        }
    }

    fun submitFeedback(rating: Int, feedback: String) {
        viewModelScope.launch {
            try {
                submitFeedbackUseCase(rating, feedback)
                    .onSuccess {
                        _state.update { it.copy(feedbackSubmitted = true) }
                        delay(3000)
                        _state.update { it.copy(feedbackSubmitted = false) }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = "Failed to submit feedback: ${e.message}") }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to submit feedback: ${e.message}") }
            }
        }
    }
} 