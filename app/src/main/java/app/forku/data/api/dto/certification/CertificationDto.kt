package app.forku.data.api.dto.certification

data class CertificationDto(
    val id: String,
    val name: String,
    val description: String?,
    val issuedDate: String,
    val expiryDate: String?,
    val status: String,
    val userId: String,
    val issuer: String,
    val certificationCode: String?,
    val documentUrl: String?,
    val timestamp: String
) 