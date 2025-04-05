package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.MaintenanceStatus
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
class SuperAdminDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(SuperAdminDashboardState())
    val state = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Add mutex to prevent concurrent loadDashboard calls
    private val loadDashboardMutex = Mutex()
    
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
                _state.update { it.copy(error = "Error loading user: ${e.message}") }
            }
        }
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            if (!loadDashboardMutex.tryLock()) return@launch
            
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Load system overview data
                val users = userRepository.getAllUsers()
                val vehicles = vehicleRepository.getAllVehicles()
                val activeAdmins = users.count { it.role == UserRole.ADMIN }
                
                // Load user management data
                val recentUsers = users.take(5)
                val pendingApprovals = users.count { !it.isApproved }
                
                // Load vehicle management data
                val maintenanceAlerts = vehicles.count { it.maintenanceStatus != MaintenanceStatus.UP_TO_DATE }
                val vehicleIssues = vehicles.count { it.hasIssues }
                
                // Load system settings data
                val systemHealth = SystemHealth(
                    lastCheckTime = java.time.LocalDateTime.now().toString()
                )
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        totalUsersCount = users.size,
                        totalVehiclesCount = vehicles.size,
                        activeAdminsCount = activeAdmins,
                        recentUsers = recentUsers,
                        pendingUserApprovals = pendingApprovals,
                        maintenanceAlerts = maintenanceAlerts,
                        vehicleIssues = vehicleIssues,
                        systemHealth = systemHealth
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error loading dashboard data: ${e.message}"
                    )
                }
            } finally {
                loadDashboardMutex.unlock()
            }
        }
    }
    
    fun submitFeedback(rating: Int, feedback: String) {
        viewModelScope.launch {
            try {
                submitFeedbackUseCase(rating, feedback)
                _state.update { it.copy(feedbackSubmitted = true) }
                
                // Reset feedback submitted state after 3 seconds
                delay(3000)
                _state.update { it.copy(feedbackSubmitted = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error submitting feedback: ${e.message}") }
            }
        }
    }
} 