package app.forku.domain.model.vehicle

data class VehicleModel(
    val id: String,
    val name: String,
    val description: String?,
    val businessId: String,
    val categoryId: String,
    val vehicleTypeId: String,
    val licensePlate: String,
    val vin: String?,
    val year: Int?,
    val make: String?,
    val model: String?,
    val color: String?,
    val status: VehicleStatus,
    val createdAt: Long,
    val updatedAt: Long
)