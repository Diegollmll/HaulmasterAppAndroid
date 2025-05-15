package app.forku.domain.model.user

import app.forku.core.auth.RoleConverter

enum class UserRole {
    SYSTEM_OWNER,
    SUPERADMIN,
    ADMIN,
    OPERATOR;

    companion object {
        fun fromString(role: String): UserRole {
            return RoleConverter.fromString(role)
        }
    }

    override fun toString(): String {
        return RoleConverter.toString(this)
    }
}