package app.forku.presentation.user.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UserManagementState())
    val state: StateFlow<UserManagementState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
        loadUsers()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase.invoke()
                _currentUser.value = user
                Log.d("UserManagement", "Current user loaded: ${user?.role}")
            } catch (e: Exception) {
                Log.e("UserManagement", "Error loading user", e)
                _state.update { it.copy(error = "Failed to load user: ${e.message}") }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val users = userRepository.getAllUsers()
                Log.d("UserManagement", "Loaded ${users.size} users")
                
                // Calculate role distribution
                val roleDistribution = users.groupBy { it.role }
                    .mapValues { it.value.size }
                
                // Count pending approvals
                val pendingApprovals = users.count { !it.isApproved }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        users = users,
                        totalUsers = users.size,
                        roleDistribution = roleDistribution,
                        pendingApprovals = pendingApprovals
                    )
                }
                Log.d("UserManagement", "State updated with users data")
            } catch (e: Exception) {
                Log.e("UserManagement", "Error loading users", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load users: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateUserRole(user: User, newRole: UserRole) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val result = userRepository.updateUserRole(user.id, newRole)
                result.onSuccess {
                    loadUsers() // Reload users to reflect changes
                }.onFailure { e ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to update user role: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update user role: ${e.message}"
                    )
                }
            }
        }
    }

    fun approveUser(user: User) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                Log.d("UserManagement", "Attempting to approve user: ${user.id}")
                
                // Create updated user with isApproved = true while preserving all other fields
                val updatedUser = user.copy(
                    isApproved = true,
                    // Preserve all other fields explicitly
                    id = user.id,
                    token = user.token,
                    refreshToken = user.refreshToken,
                    email = user.email,
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    photoUrl = user.photoUrl,
                    role = user.role,
                    certifications = user.certifications,
                    points = user.points,
                    totalHours = user.totalHours,
                    totalDistance = user.totalDistance,
                    sessionsCompleted = user.sessionsCompleted,
                    incidentsReported = user.incidentsReported,
                    lastMedicalCheck = user.lastMedicalCheck,
                    lastLogin = user.lastLogin,
                    isActive = user.isActive,
                    password = user.password,
                    businessId = user.businessId,
                    siteId = user.siteId,
                    systemOwnerId = user.systemOwnerId
                )
                
                // Update the user
                userRepository.updateUser(updatedUser)
                
                Log.d("UserManagement", "User approved successfully")
                
                // Reload users to reflect changes
                loadUsers()
            } catch (e: Exception) {
                Log.e("UserManagement", "Error approving user", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to approve user: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                userRepository.deleteUser(userId)
                loadUsers() // Reload users to reflect changes
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete user: ${e.message}"
                    )
                }
            }
        }
    }

    fun showRoleDialog(user: User) {
        _state.update { 
            it.copy(
                selectedUser = user,
                showRoleDialog = true
            )
        }
    }

    fun hideRoleDialog() {
        _state.update { 
            it.copy(
                selectedUser = null,
                showRoleDialog = false
            )
        }
    }

    fun toggleAddUserDialog() {
        _state.update { it.copy(showAddUserDialog = !it.showAddUserDialog) }
    }

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                Log.d("UserManagement", "Attempting to register new user: $firstName $lastName with role $role")
                
                val result = userRepository.register(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password
                )
                
                result.onSuccess { user ->
                    // Update the user's role after successful registration
                    userRepository.updateUserRole(user.id, role).onSuccess {
                        Log.d("UserManagement", "User registered and role updated successfully")
                        loadUsers() // Reload the users list
                    }.onFailure { e ->
                        Log.e("UserManagement", "Failed to update user role", e)
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to set user role: ${e.message}"
                            )
                        }
                    }
                }.onFailure { e ->
                    Log.e("UserManagement", "Failed to register user", e)
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to register user: ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("UserManagement", "Error in registerUser", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error registering user: ${e.message}"
                    )
                }
            }
        }
    }
} 