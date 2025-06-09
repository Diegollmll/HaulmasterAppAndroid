package app.forku.core.auth

import android.util.Log
import app.forku.domain.model.user.UserRole

object RoleConverter {
    /**
     * Converts a string role to UserRole enum
     * @param roleString The role string to convert
     * @param defaultRole The default role to return if conversion fails (defaults to OPERATOR)
     * @return The corresponding UserRole
     */
    fun fromString(roleString: String?, defaultRole: UserRole = UserRole.OPERATOR): UserRole {
        Log.d("RoleConverter", "=== Converting role string ===")
        Log.d("RoleConverter", "Input roleString: '$roleString'")
        Log.d("RoleConverter", "Default role: $defaultRole")
        
        if (roleString == null) {
            Log.d("RoleConverter", "Role string is null, returning default: $defaultRole")
            return defaultRole
        }
        
        val normalized = roleString.trim().lowercase().replace(" ", "")
        Log.d("RoleConverter", "Normalized role string: '$normalized'")
        
        return try {
            val result = when (normalized) {
                "systemowner", "system_owner" -> UserRole.SYSTEM_OWNER
                "superadmin", "super_admin" -> UserRole.SUPERADMIN
                "admin", "administrator", "administrador" -> UserRole.ADMIN
                "operator", "operador" -> UserRole.OPERATOR
                else -> {
                    Log.d("RoleConverter", "No direct match, trying enum parse...")
                    // Try to parse as enum name directly
                    try {
                        val enumResult = UserRole.valueOf(roleString.uppercase())
                        Log.d("RoleConverter", "Enum parse successful: $enumResult")
                        enumResult
                    } catch (e: IllegalArgumentException) {
                        Log.w("RoleConverter", "Could not convert role string: '$roleString', using default role: $defaultRole")
                        defaultRole
                    }
                }
            }
            Log.d("RoleConverter", "Final conversion result: $result")
            result
        } catch (e: Exception) {
            Log.e("RoleConverter", "Error converting role string: '$roleString'", e)
            defaultRole
        }
    }

    /**
     * Converts a UserRole to its corresponding API string representation
     * @param role The UserRole to convert
     * @return The string representation for the API
     */
    fun toString(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> "systemowner"
            UserRole.SUPERADMIN -> "superadmin"
            UserRole.ADMIN -> "administrator"
            UserRole.OPERATOR -> "operator"
        }
    }

    fun getDashboardRouteForRole(role: UserRole): String {
        return when (role) {
            UserRole.SYSTEM_OWNER -> app.forku.presentation.navigation.Screen.SystemOwnerDashboard.route
            UserRole.SUPERADMIN -> app.forku.presentation.navigation.Screen.SuperAdminDashboard.route
            UserRole.ADMIN -> app.forku.presentation.navigation.Screen.AdminDashboard.route
            UserRole.OPERATOR -> app.forku.presentation.navigation.Screen.Dashboard.route
        }
    }
} 