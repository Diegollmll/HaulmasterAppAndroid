package app.forku.data.api.dto

data class OperatorCertificationDto (
    val vehicleTypeId: String,
    val isValid: Boolean,
    val expiresAt: String
)