package app.forku.domain.model.certification

data class CertificationVehicleType(
    val id: String,
    val certificationId: String,
    val vehicleTypeId: String,
    val vehicleTypeName: String? = null, // Para evitar joins cuando solo necesitamos el nombre
    val businessId: String? = null,
    val siteId: String? = null,
    val timestamp: String,
    val isMarkedForDeletion: Boolean = false,
    val isDirty: Boolean = false,
    val isNew: Boolean = false,
    val internalObjectId: Int = 0
) 