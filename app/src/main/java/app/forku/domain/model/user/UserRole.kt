package app.forku.domain.model.user

import app.forku.core.auth.UserRoleManager

enum class UserRole {
    SYSTEM_OWNER,
    SUPERADMIN,
    ADMIN,
    OPERATOR;

    companion object {
        fun fromString(role: String): UserRole {
            return UserRoleManager.fromString(role)
        }
    }

    override fun toString(): String {
        return UserRoleManager.toApiString(this)
    }
}