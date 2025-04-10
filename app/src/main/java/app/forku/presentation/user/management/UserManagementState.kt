package app.forku.presentation.user.management

import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole

data class UserManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val users: List<User> = emptyList(),
    val totalUsers: Int = 0,
    val pendingApprovals: Int = 0,
    val roleDistribution: Map<UserRole, Int> = emptyMap(),
    val showAddUserDialog: Boolean = false,
    val selectedUser: User? = null,
    val showRoleDialog: Boolean = false
) 