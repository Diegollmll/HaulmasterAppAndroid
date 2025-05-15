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
        if (roleString == null) return defaultRole
        
        return try {
            when (roleString.lowercase()) {
                "systemowner" -> UserRole.SYSTEM_OWNER
                "superadmin" -> UserRole.SUPERADMIN
                "administrator" -> UserRole.ADMIN
                "operator" -> UserRole.OPERATOR
                else -> {
                    // Try to parse as enum name directly
                    try {
                        UserRole.valueOf(roleString.uppercase())
                    } catch (e: IllegalArgumentException) {
                        Log.w("RoleConverter", "Could not convert role string: $roleString, using default role")
                        defaultRole
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RoleConverter", "Error converting role string: $roleString", e)
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
} 