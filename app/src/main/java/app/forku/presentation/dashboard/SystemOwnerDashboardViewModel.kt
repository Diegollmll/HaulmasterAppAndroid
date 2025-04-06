package app.forku.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.MaintenanceStatus
import app.forku.domain.model.vehicle.Vehicle
import app.forku.presentation.dashboard.Business
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
class SystemOwnerDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val businessRepository: BusinessRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(SystemOwnerDashboardState())
    val state = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

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
                Log.d("SystemOwnerDashboard", "Current user loaded: ${user?.role}")
            } catch (e: Exception) {
                Log.e("SystemOwnerDashboard", "Error loading current user", e)
                _state.update { it.copy(error = "Error loading user: ${e.message}") }
            }
        }
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            if (!loadDashboardMutex.tryLock()) {
                Log.d("SystemOwnerDashboard", "Dashboard data load already in progress")
                return@launch
            }
            
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                Log.d("SystemOwnerDashboard", "Starting dashboard data load")
                
                // Load current user first
                val currentUser = userRepository.getCurrentUser()
                Log.d("SystemOwnerDashboard", "Current user: role=${currentUser?.role}, id=${currentUser?.id}")
                
                // Load businesses first since they're critical
                val businesses = try {
                    businessRepository.getAllBusinesses().also { businessList ->
                        Log.d("SystemOwnerDashboard", "Successfully loaded ${businessList.size} businesses")
                        businessList.forEach { business ->
                            Log.d("SystemOwnerDashboard", "Business loaded: id=${business.id}, name=${business.name}, status=${business.status}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SystemOwnerDashboard", "Error loading businesses", e)
                    emptyList()
                }

                // Calculate business statistics
                val activeBusinesses = businesses.count { it.status == BusinessStatus.ACTIVE }
                val pendingBusinesses = businesses.count { it.status == BusinessStatus.PENDING }
                val suspendedBusinesses = businesses.count { it.status == BusinessStatus.SUSPENDED }
                
                Log.d("SystemOwnerDashboard", "Business statistics: " +
                    "total=${businesses.size}, " +
                    "active=$activeBusinesses, " +
                    "pending=$pendingBusinesses, " +
                    "suspended=$suspendedBusinesses")

                // Load users
                val users = try {
                    userRepository.getAllUsers().also {
                        Log.d("SystemOwnerDashboard", "Successfully loaded ${it.size} users")
                    }
                } catch (e: Exception) {
                    Log.e("SystemOwnerDashboard", "Error loading users", e)
                    emptyList()
                }

                // Load vehicles
                val vehicles = try {
                    vehicleRepository.getAllVehicles().also {
                        Log.d("SystemOwnerDashboard", "Successfully loaded ${it.size} vehicles")
                    }
                } catch (e: Exception) {
                    Log.e("SystemOwnerDashboard", "Error loading vehicles", e)
                    emptyList()
                }

                // Update state with all data
                _state.update { currentState ->
                    Log.d("SystemOwnerDashboard", "Updating dashboard state with new data")
                    currentState.copy(
                        isLoading = false,
                        error = null,
                        totalUsersCount = users.size,
                        totalVehiclesCount = vehicles.size,
                        totalBusinessCount = businesses.size,
                        activeBusinesses = activeBusinesses,
                        pendingBusinesses = pendingBusinesses,
                        suspendedBusinesses = suspendedBusinesses,
                        recentBusinesses = businesses.take(5),
                        totalAdminsCount = users.count { it.role == UserRole.ADMIN },
                        totalSuperAdminsCount = users.count { it.role == UserRole.SUPERADMIN },
                        roleDistribution = users.groupBy { it.role }.mapValues { it.value.size },
                        recentUsers = users.take(5),
                        pendingUserApprovals = users.count { !it.isApproved }
                    )
                }
                Log.d("SystemOwnerDashboard", "Dashboard state updated successfully")
                
            } catch (e: Exception) {
                Log.e("SystemOwnerDashboard", "Error loading dashboard data", e)
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
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error submitting feedback: ${e.message}") }
            }
        }
    }
} 