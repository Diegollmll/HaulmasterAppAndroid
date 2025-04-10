package app.forku.data.api.dto.user

data class UserDto(
    val id: String,
    val token: String,
    val refreshToken: String,
    val email: String,
    val password: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String?,
    val role: String,
    val certifications: List<CertificationDto>,
    val lastMedicalCheck: String?,
    val lastLogin: String?,
    val isActive: Boolean = true,
    val isApproved: Boolean = false,
    val businessId: String? = null,
    val systemOwnerId: String? = null
)

data class CertificationDto(
    val vehicleTypeId: String,
    val isValid: Boolean,
    val expiresAt: String
)