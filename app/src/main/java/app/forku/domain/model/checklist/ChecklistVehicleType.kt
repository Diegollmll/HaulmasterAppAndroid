package app.forku.domain.model.checklist

data class ChecklistVehicleType(
    val id: String,
    val checklistId: String,
    val vehicleTypeId: String,
    val isMarkedForDeletion: Boolean = false,
    val internalObjectId: Int = 0
) 