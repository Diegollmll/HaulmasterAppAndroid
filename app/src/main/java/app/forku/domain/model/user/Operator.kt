package app.forku.domain.model.user

import app.forku.domain.model.vehicle.VehicleType

data class Operator(
    val user: User,
    val name: String,
    val isTrainer: Boolean,
    val isCertified: Boolean,
    val experienceYears: Int,
    val lastMedicalCheck: String?,
    val emergencyContact: String?,
    val emergencyPhone: String?
) {
    // Delegación de propiedades de User
    val id: String get() = user.id
    val username: String get() = user.username
    val role: UserRole get() = user.role
    val permissions: List<String> get() = user.permissions
    val certifications: List<Certification> get() = user.certifications

    fun canOperateVehicle(vehicleType: VehicleType): Boolean {
        return isCertified && certifications.any {
            it.vehicleTypeId == vehicleType.id && it.isValid
        }
    }

    fun hasValidMedicalCheck(): Boolean {
        return !lastMedicalCheck.isNullOrEmpty()
    }

    fun isQualifiedForTraining(): Boolean {
        return experienceYears >= 2 && isCertified
    }


}