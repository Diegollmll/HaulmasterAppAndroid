package app.forku.domain.model.user

enum class UserRole {
    ADMIN,
    OPERATOR,
    USER;

    companion object {
        fun fromString(role: String): UserRole {
            return valueOf(role.uppercase())
        }
    }

    override fun toString(): String {
        return name
    }
}