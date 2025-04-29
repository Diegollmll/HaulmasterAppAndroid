package app.forku.presentation.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.model.business.BusinessStatus
import app.forku.presentation.dashboard.Business
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import app.forku.data.datastore.AuthDataStore
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.user.UserBusinessRepository

@HiltViewModel
class BusinessManagementViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val userBusinessRepository: UserBusinessRepository,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessManagementState())
    val state: StateFlow<BusinessManagementState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _availableSuperAdmins = MutableStateFlow<List<User>>(emptyList())
    val availableSuperAdmins: StateFlow<List<User>> = _availableSuperAdmins.asStateFlow()

    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses = _businesses.asStateFlow()

    private val _businessSuperAdmins = MutableStateFlow<Map<String, User>>(emptyMap())
    val businessSuperAdmins = _businessSuperAdmins.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadCurrentUser()
        loadBusinesses()
        loadSuperAdmins()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
                Log.d("BusinessManagement", "Current user loaded: ${user?.role}")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error loading user", e)
                _state.update { it.copy(error = "Failed to load user: ${e.message}") }
            }
        }
    }

    private fun loadSuperAdmins() {
        viewModelScope.launch {
            try {
                val superAdmins = userRepository.getUsersByRole(UserRole.SUPERADMIN)
                _availableSuperAdmins.value = superAdmins
                Log.d("BusinessManagement", "Loaded ${superAdmins.size} SuperAdmins")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error loading SuperAdmins", e)
            }
        }
    }

    fun loadBusinesses() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val businessList = businessRepository.getAllBusinesses()
                _businesses.value = businessList
                
                // Load SuperAdmins for each business
                val superAdminsMap = mutableMapOf<String, User>()
                businessList.forEach { business ->
                    userBusinessRepository.getBusinessSuperAdmin(business.id)?.let { superAdmin ->
                        superAdminsMap[business.id] = superAdmin
                    }
                }
                _businessSuperAdmins.value = superAdminsMap
                
            } catch (e: Exception) {
                _error.value = "Failed to load businesses: ${e.message}"
            } finally {
                _loading.value = false
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

    fun updateBusinessStatus(business: Business, newStatus: BusinessStatus) {
        viewModelScope.launch {
            try {
                businessRepository.updateBusinessStatus(business.id, newStatus)
                // Update local state
                _businesses.value = _businesses.value.map { 
                    if (it.id == business.id) it.copy(status = newStatus) else it 
                }
            } catch (e: Exception) {
                _error.value = "Failed to update business status: ${e.message}"
            }
        }
    }

    fun showAssignUsersDialog(business: Business) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Log.d("BusinessManagement", "Loading users for business ${business.id}")
                
                // Load unassigned users
                val unassignedUsers = userRepository.getUnassignedUsers()
                    .filter { user -> 
                        user.role != UserRole.SYSTEM_OWNER && 
                        user.role != UserRole.SUPERADMIN 
                    }
                Log.d("BusinessManagement", "Found ${unassignedUsers.size} unassigned users")

                // Load current business users
                val currentBusinessUserIds = businessRepository.getBusinessUsers(business.id)
                val currentBusinessUsers = currentBusinessUserIds.mapNotNull { userId ->
                    userRepository.getUserById(userId)
                }.filter { user ->
                    user.role != UserRole.SYSTEM_OWNER &&
                    user.role != UserRole.SUPERADMIN
                }
                Log.d("BusinessManagement", "Found ${currentBusinessUsers.size} current business users")

                // Combine both lists for the dialog
                val availableUsers = (unassignedUsers + currentBusinessUsers).distinctBy { it.id }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        showAssignUsersDialog = true,
                        selectedBusinessForUserAssignment = business,
                        availableUsers = availableUsers,
                        selectedUsers = currentBusinessUserIds,
                        error = null
                    )
                }
                Log.d("BusinessManagement", "Updated state with users for assignment dialog")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error loading users for assignment", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load users: ${e.message}"
                    )
                }
            }
        }
    }

    fun hideAssignUsersDialog() {
        _state.update { 
            it.copy(
                showAssignUsersDialog = false,
                selectedBusinessForUserAssignment = null,
                availableUsers = emptyList(),
                selectedUsers = emptyList(),
                error = null
            )
        }
        Log.d("BusinessManagement", "Closed user assignment dialog")
    }

    fun toggleUserSelection(userId: String) {
        _state.update { currentState ->
            val selectedUsers = currentState.selectedUsers.toMutableList()
            if (userId in selectedUsers) {
                selectedUsers.remove(userId)
                Log.d("BusinessManagement", "Unselected user: $userId")
            } else {
                selectedUsers.add(userId)
                Log.d("BusinessManagement", "Selected user: $userId")
            }
            currentState.copy(selectedUsers = selectedUsers)
        }
    }

    fun saveUserAssignments() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val business = _state.value.selectedBusinessForUserAssignment
                if (business == null) {
                    Log.e("BusinessManagement", "No business selected for user assignment")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "No business selected for user assignment"
                        )
                    }
                    return@launch
                }

                Log.d("BusinessManagement", "Saving user assignments for business: ${business.id}")
                Log.d("BusinessManagement", "Selected users: ${_state.value.selectedUsers}")

                // Get current business users
                val currentUsers = businessRepository.getBusinessUsers(business.id)
                Log.d("BusinessManagement", "Current business users: $currentUsers")
                
                // Remove users that were unselected
                currentUsers.forEach { userId ->
                    if (userId !in _state.value.selectedUsers) {
                        Log.d("BusinessManagement", "Removing user $userId from business ${business.id}")
                        businessRepository.removeUserFromBusiness(userId, business.id)
                    }
                }
                
                // Add newly selected users
                _state.value.selectedUsers.forEach { userId ->
                    if (userId !in currentUsers) {
                        Log.d("BusinessManagement", "Assigning user $userId to business ${business.id}")
                        businessRepository.assignUserToBusiness(userId, business.id)
                    }
                }
                
                Log.d("BusinessManagement", "Successfully saved user assignments")
                loadBusinesses() // Reload to update counts
                hideAssignUsersDialog()
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error saving user assignments", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to save user assignments: ${e.message}"
                    )
                }
            }
        }
    }

    fun assignSuperAdmin(businessId: String, userId: String) {
        viewModelScope.launch {
            try {
                userBusinessRepository.assignSuperAdmin(businessId, userId)
                // Refresh the SuperAdmin mapping
                userRepository.getUserById(userId)?.let { superAdmin ->
                    _businessSuperAdmins.value = _businessSuperAdmins.value + (businessId to superAdmin)
                }
            } catch (e: Exception) {
                _error.value = "Failed to assign SuperAdmin: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 