package app.forku.core.auth

import android.util.Log
import app.forku.domain.model.user.UserRole

/**
 * Legacy wrapper for UserRoleManager
 * 
 * @deprecated Use UserRoleManager directly for new code
 * This class is maintained for backward compatibility only
 */
object RoleConverter {
    
    /**
     * @deprecated Use UserRoleManager.fromString() instead
     */
    @Deprecated("Use UserRoleManager.fromString() instead", ReplaceWith("UserRoleManager.fromString(roleString, defaultRole)"))
    fun fromString(roleString: String?, defaultRole: UserRole = UserRole.OPERATOR): UserRole {
        return UserRoleManager.fromString(roleString, defaultRole)
    }

    /**
     * @deprecated Use UserRoleManager.toApiString() instead
     */
    @Deprecated("Use UserRoleManager.toApiString() instead", ReplaceWith("UserRoleManager.toApiString(role)"))
    fun toString(role: UserRole): String {
        return UserRoleManager.toApiString(role)
    }

    /**
     * @deprecated Use UserRoleManager.toDisplayString() instead
     */
    @Deprecated("Use UserRoleManager.toDisplayString() instead", ReplaceWith("UserRoleManager.toDisplayString(role)"))
    fun toDisplayString(role: UserRole): String {
        return UserRoleManager.toDisplayString(role)
    }

    /**
     * @deprecated Use UserRoleManager.getDashboardRoute() instead
     */
    @Deprecated("Use UserRoleManager.getDashboardRoute() instead", ReplaceWith("UserRoleManager.getDashboardRoute(role)"))
    fun getDashboardRouteForRole(role: UserRole): String {
        return UserRoleManager.getDashboardRoute(role)
    }
} 