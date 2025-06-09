package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.MaintenanceStatus
import app.forku.domain.model.business.BusinessStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.delay
import android.util.Log


@HiltViewModel
class SuperAdminDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val businessRepository: BusinessRepository,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(SuperAdminDashboardState())
    val state: StateFlow<SuperAdminDashboardState> = _state.asStateFlow()
    
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
                Log.d("SuperAdminDashboard", "Current user loaded: ${user?.role}")
            } catch (e: Exception) {
                Log.e("SuperAdminDashboard", "Error loading user", e)
                _state.update { it.copy(error = "Error loading user: ${e.message}") }
            }
        }
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            if (!loadDashboardMutex.tryLock()) {
                Log.d("SuperAdminDashboard", "Dashboard data load already in progress")
                return@launch
            }
            
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                Log.d("SuperAdminDashboard", "Starting dashboard data load")
                
                // Load current user first
                val currentUser = userRepository.getCurrentUser()
                Log.d("SuperAdminDashboard", "Current user: role=${currentUser?.role}, id=${currentUser?.id}")
                
                // Load businesses for current SuperAdmin
                val businesses = try {
                    businessRepository.getAllBusinesses().also { businessList ->
                        Log.d("SuperAdminDashboard", "Successfully loaded ${businessList.size} businesses for SuperAdmin")
                        businessList.forEach { business ->
                            Log.d("SuperAdminDashboard", "Business loaded: id=${business.id}, name=${business.name}, status=${business.status}, superAdminId=${business.superAdminId ?: "none"}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SuperAdminDashboard", "Error loading businesses", e)
                    emptyList()
                }

                // Get business IDs for this SuperAdmin
                val superAdminBusinessIds = businesses
                    .filter { business -> 
                        currentUser?.id == business.superAdminId || currentUser?.role == UserRole.SYSTEM_OWNER
                    }
                    .map { it.id }
                    .toSet()
                Log.d("SuperAdminDashboard", "SuperAdmin business IDs: $superAdminBusinessIds")

                // Load and filter users belonging to SuperAdmin's businesses
                val users = try {
                    userRepository.getAllUsers()
                        .filter { user -> 
                            user.businessId in superAdminBusinessIds
                        }
                        .also { filteredUsers ->
                            Log.d("SuperAdminDashboard", "Filtered ${filteredUsers.size} users belonging to SuperAdmin's businesses")
                            filteredUsers.forEach { user ->
                                Log.d("SuperAdminDashboard", "Filtered user: id=${user.id}, businessId=${user.businessId}")
                            }
                        }
                } catch (e: Exception) {
                    Log.e("SuperAdminDashboard", "Error loading users", e)
                    emptyList()
                }

                // Load and filter vehicles belonging to SuperAdmin's businesses
                val vehicles = try {
                    val allVehicles = vehicleRepository.getAllVehicles()
                    allVehicles.filter { vehicle -> 
                        // Get the business for this vehicle and check if it belongs to the SuperAdmin
                        val businessId = vehicle.businessId
                        businessId in superAdminBusinessIds
                    }.also { filteredVehicles ->
                        Log.d("SuperAdminDashboard", "Filtered ${filteredVehicles.size} vehicles belonging to SuperAdmin's businesses")
                    }
                } catch (e: Exception) {
                    Log.e("SuperAdminDashboard", "Error loading vehicles", e)
                    emptyList()
                }

                // Calculate business statistics
                val businessesByStatus = businesses.groupBy { it.status }
                    .mapValues { it.value.size }
                
                Log.d("SuperAdminDashboard", "Business statistics: " +
                    "total=${businesses.size}, " +
                    "active=${businessesByStatus[BusinessStatus.ACTIVE] ?: 0}, " +
                    "pending=${businessesByStatus[BusinessStatus.PENDING] ?: 0}")

                // Update state with filtered data
                _state.update { currentState ->
                    Log.d("SuperAdminDashboard", "Updating dashboard state with filtered data")
                    currentState.copy(
                        isLoading = false,
                        error = null,
                        totalUsersCount = users.size,
                        totalVehiclesCount = vehicles.size,
                        totalBusinessCount = businesses.size,
                        activeAdminsCount = users.count { it.role == UserRole.ADMIN },
                        recentUsers = users.take(5),
                        recentBusinesses = businesses.take(5),
                        businesses = businesses,
                        totalBusinesses = businesses.size,
                        businessesByStatus = businessesByStatus
                    )
                }
                Log.d("SuperAdminDashboard", "Dashboard state updated successfully")
                
            } catch (e: Exception) {
                Log.e("SuperAdminDashboard", "Error loading dashboard data", e)
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
    
    fun submitFeedback(rating: Int, feedback: String, canContactMe: Boolean) {
        viewModelScope.launch {
            try {
                submitFeedbackUseCase(rating, feedback, canContactMe)
                _state.update { it.copy(feedbackSubmitted = true) }
                // Reset feedback submitted state after 3 seconds
                delay(3000)
                _state.update { it.copy(feedbackSubmitted = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error submitting feedback: ${e.message}") }
            }
        }
    }

    fun refreshDashboard() {
        loadDashboardData()
    }
} 