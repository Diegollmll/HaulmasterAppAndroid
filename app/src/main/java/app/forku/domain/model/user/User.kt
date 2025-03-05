package app.forku.domain.model.user

data class User(
    val id: String,
    val token: String,
    val refreshToken: String,
    val email: String,
    val username: String,
    val name: String,
    val photoUrl: String?,
    val role: UserRole,
    val permissions: List<String>,
    val certifications: List<Certification>,
    val lastMedicalCheck: String? = null,
    val lastLogin: String? = null,
    val isActive: Boolean = true
)