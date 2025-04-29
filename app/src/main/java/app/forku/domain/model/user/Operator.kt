package app.forku.domain.model.user

import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.certification.Certification

data class Operator(
    val user: User,
    val name: String,
    val isTrainer: Boolean = false,
    val isCertified: Boolean = false,
    val experienceLevel: String,
    val experienceYears: Int = 0,
    val points: Int,
    val totalHours: Float,
    val totalDistance: Int,
    val tasksCompleted: Int,
    val incidentsReported: Int,
    val lastMedicalCheck: String? = null
) {
    // Delegación de propiedades de User
    val id: String get() = user.id
    val username: String get() = user.username
    val role: UserRole get() = user.role
    val certifications: List<Certification> get() = user.certifications


}