package app.forku.domain.model.vehicle

data class VehicleCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val requiresCertification: Boolean = false
) 