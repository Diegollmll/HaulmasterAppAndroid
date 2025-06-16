package app.forku.domain.model.user

import app.forku.domain.model.certification.Certification

data class User(
    val id: String,
    val token: String,
    val refreshToken: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String?,
    val role: UserRole,
    val certifications: List<Certification>,
    val points: Int = 0,
    val totalHours: Float = 0f,
    val totalDistance: Int = 0,
    val sessionsCompleted: Int = 0,
    val incidentsReported: Int = 0,
    val lastMedicalCheck: String? = null,
    val lastLogin: String? = null,
    val isActive: Boolean = true,
    val isApproved: Boolean = false,
    val password: String,
    val businessId: String? = null,
    val siteId: String? = null,
    val systemOwnerId: String? = null,
    val userPreferencesId: String? = null
) {
    val fullName: String
        get() = "$firstName $lastName"
}