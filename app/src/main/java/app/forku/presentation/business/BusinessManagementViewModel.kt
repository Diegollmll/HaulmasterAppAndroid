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
import app.forku.domain.model.user.UserRole

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

    private val _availableSuperAdmins = MutableStateFlow<List<User>>(emptyList())
    val availableSuperAdmins: StateFlow<List<User>> = _availableSuperAdmins.asStateFlow()

    private val _businessSuperAdmins = MutableStateFlow<Map<String, User?>>(emptyMap())
    val businessSuperAdmins: StateFlow<Map<String, User?>> = _businessSuperAdmins.asStateFlow()

    init {
        loadCurrentUser()
        loadBusinesses()
        loadSuperAdmins()
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

    private fun loadBusinessSuperAdmins(businesses: List<Business>) {
        viewModelScope.launch {
            try {
                val superAdminsMap = mutableMapOf<String, User?>()
                businesses.forEach { business ->
                    if (business.superAdminId != null) {
                        val superAdmin = userRepository.getUserById(business.superAdminId)
                        superAdminsMap[business.id] = superAdmin
                    }
                }
                _businessSuperAdmins.value = superAdminsMap
                Log.d("BusinessManagement", "Loaded SuperAdmins for ${superAdminsMap.size} businesses")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error loading business SuperAdmins", e)
            }
        }
    }

    fun loadBusinesses() {
        viewModelScope.launch {
            try {
                Log.d("BusinessManagement", "Starting loadBusinesses function")
                _state.update { it.copy(isLoading = true, error = null) }
                
                val businesses = businessRepository.getAllBusinesses()
                Log.d("BusinessManagement", "Successfully loaded ${businesses.size} businesses")
                
                // Load SuperAdmins for businesses
                loadBusinessSuperAdmins(businesses)
                
                // Get unassigned users excluding SYSTEM_OWNER and SUPERADMIN
                val unassignedUsers = userRepository.getUnassignedUsers()
                    .count { user -> 
                        user.role != UserRole.SYSTEM_OWNER && 
                        user.role != UserRole.SUPERADMIN 
                    }
                Log.d("BusinessManagement", "Unassigned users (excluding SYSTEM_OWNER and SUPERADMIN): $unassignedUsers")
                
                val totalBusinesses = businesses.size
                val pendingApprovals = businesses.count { it.status == BusinessStatus.PENDING }
                
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        businesses = businesses,
                        totalBusinesses = totalBusinesses,
                        pendingApprovals = pendingApprovals,
                        unassignedUsers = unassignedUsers,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error in loadBusinesses", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load businesses: ${e.message}"
                    )
                }
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
                Log.d("BusinessManagement", "Updating business ${business.id} status to $newStatus")
                _state.update { it.copy(isLoading = true, error = null) }
                
                val currentUser = getCurrentUserUseCase.invoke()
                if (currentUser?.role != UserRole.SYSTEM_OWNER) {
                    Log.e("BusinessManagement", "Unauthorized attempt to change business status by role: ${currentUser?.role}")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Only System Owner can change business status"
                        )
                    }
                    return@launch
                }

                val updatedBusiness = business.copy(status = newStatus)
                businessRepository.updateBusiness(updatedBusiness)
                
                Log.d("BusinessManagement", "Business status updated successfully")
                loadBusinesses() // Reload the list to reflect changes
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error updating business status", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update business status: ${e.message}"
                    )
                }
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

    fun assignSuperAdmin(businessId: String, superAdminId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Log.d("BusinessManagement", "Starting SuperAdmin assignment - Business: $businessId, SuperAdmin: $superAdminId")
                
                // Get current business first
                val business = businessRepository.getBusinessById(businessId)
                Log.d("BusinessManagement", "Current business state - Name: ${business.name}, Status: ${business.status}, CurrentSuperAdmin: ${business.superAdminId}")
                
                // Attempt to transfer business
                businessRepository.transferBusinessToSuperAdmin(businessId, superAdminId)
                
                // Reload businesses to reflect changes
                loadBusinesses()
                
                _state.update { it.copy(
                    isLoading = false,
                    error = null
                ) }
                
                Log.d("BusinessManagement", "Successfully assigned SuperAdmin to business")
            } catch (e: Exception) {
                Log.e("BusinessManagement", "Error assigning SuperAdmin", e)
                _state.update { it.copy(
                    isLoading = false,
                    error = "Failed to assign SuperAdmin: ${e.message}"
                ) }
            }
        }
    }
} 