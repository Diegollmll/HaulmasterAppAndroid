package app.forku.domain.model.user

data class Certification(
    val vehicleTypeId: String,
    val isValid: Boolean,
    val expiresAt: String
) 