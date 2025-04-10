package app.forku.domain.model.vehicle

data class VehicleType(
    val id: String,
    val name: String,
    val description: String? = null,
    val categoryId: String,
    val maxWeight: Double? = null,
    val maxPassengers: Int? = null,
    val requiresSpecialLicense: Boolean = false,
    val requiresCertification: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
