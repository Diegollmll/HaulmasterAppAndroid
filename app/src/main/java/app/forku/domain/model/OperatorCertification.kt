package app.forku.domain.model

data class OperatorCertification(
    val vehicleTypeId: String,
    val isValid: Boolean,
    val expiresAt: String
)