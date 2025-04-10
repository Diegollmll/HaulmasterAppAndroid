package app.forku.domain.model.vehicle

data class VehicleCategoryModel(
    val id: String,
    val name: String,
    val description: String?,
    val requiresCertification: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) 