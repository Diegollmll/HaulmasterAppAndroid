package app.forku.presentation.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.user.GetCurrentUserUseCase
import app.forku.presentation.dashboard.Business
import app.forku.presentation.dashboard.BusinessStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class BusinessManagementViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessManagementState())
    val state: StateFlow<BusinessManagementState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
        loadBusinesses()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase.invoke()
                _currentUser.value = user
                Log.d("BusinessManagement", "Current user loaded: ${user?.role}")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error loading user", e)
                _state.update { it.copy(error = "Failed to load user: ${e.message}") }
            }
        }
    }

    fun loadBusinesses() {
        viewModelScope.launch {
            try {
                Log.d("BusinessManagement", "Starting loadBusinesses function")
                _state.update { it.copy(isLoading = true, error = null) }
                Log.d("BusinessManagement", "State updated: isLoading = true")
                
                // Load businesses
                Log.d("BusinessManagement", "Calling businessRepository.getAllBusinesses()")
                val businesses = businessRepository.getAllBusinesses()
                Log.d("BusinessManagement", "Successfully loaded ${businesses.size} businesses")
                
                // Load unassigned users count
                Log.d("BusinessManagement", "Calling userRepository.getUnassignedUsers()")
                val unassignedUsers = userRepository.getUnassignedUsers().size
                Log.d("BusinessManagement", "Found $unassignedUsers unassigned users")
                
                // Calculate statistics
                Log.d("BusinessManagement", "Calculating statistics")
                val totalBusinesses = businesses.size
                val pendingApprovals = businesses.count { it.status == BusinessStatus.PENDING }
                Log.d("BusinessManagement", "Statistics calculated - Total: $totalBusinesses, Pending: $pendingApprovals")
                
                _state.update { currentState ->
                    Log.d("BusinessManagement", "Updating state with new data")
                    currentState.copy(
                        isLoading = false,
                        businesses = businesses,
                        totalBusinesses = totalBusinesses,
                        pendingApprovals = pendingApprovals,
                        unassignedUsers = unassignedUsers,
                        error = null
                    )
                }
                Log.d("BusinessManagement", "State successfully updated with ${businesses.size} businesses")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error in loadBusinesses", e)
                Log.e("BusinessManagement", "Error type: ${e.javaClass.simpleName}")
                Log.e("BusinessManagement", "Error message: ${e.message}")
                Log.e("BusinessManagement", "Error stack trace: ${e.stackTraceToString()}")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load businesses: ${e.message}"
                    )
                }
                Log.d("BusinessManagement", "Error state updated")
            }
        }
    }

    fun showAddBusinessDialog() {
        Log.d("BusinessManagement", "Showing add business dialog")
        _state.update { it.copy(showAddBusinessDialog = true) }
    }

    fun hideAddBusinessDialog() {
        Log.d("BusinessManagement", "Hiding add business dialog")
        _state.update { it.copy(showAddBusinessDialog = false) }
    }

    fun addBusiness(name: String) {
        Log.d("BusinessManagement", "Adding business with name: $name")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Log.d("BusinessManagement", "Calling repository to create business")
                businessRepository.createBusiness(name)
                Log.d("BusinessManagement", "Business created successfully")
                loadBusinesses() // Reload the list after adding
                hideAddBusinessDialog()
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error creating business", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create business: ${e.message}"
                    )
                }
            }
        }
    }
} 