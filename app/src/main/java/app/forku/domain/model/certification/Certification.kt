package app.forku.domain.model.certification

data class Certification(
    val id: String,
    val name: String,
    val description: String?,
    val issuedDate: String,
    val expiryDate: String?,
    val status: CertificationStatus,
    val userId: String,
    val issuer: String,
    val certificationCode: String?,
    val documentUrl: String?,
    val timestamp: String,
    val isMarkedForDeletion: Boolean = false,
    val isDirty: Boolean = false,
    val isNew: Boolean = false,
    val internalObjectId: Int = 0
)

enum class CertificationStatus {
    ACTIVE,
    EXPIRED,
    PENDING,
    REVOKED;

    companion object {
        fun fromString(value: String): CertificationStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                PENDING
            }
        }
    }
} 