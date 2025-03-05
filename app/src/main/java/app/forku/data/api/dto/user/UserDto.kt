package app.forku.data.api.dto.user

data class UserDto(
    val id: String,
    val token: String,
    val refreshToken: String,
    val email: String,
    val password: String,
    val username: String,
    val name: String,
    val photoUrl: String?,
    val role: String,
    val permissions: List<String>,
    val certifications: List<CertificationDto>,
    val last_medical_check: String?,
    val last_login: String?,
    val is_active: Boolean = true
)

data class CertificationDto(
    val vehicleTypeId: String,
    val isValid: Boolean,
    val expiresAt: String
)