package app.forku.domain.model.checklist

data class ChecklistQuestionVehicleType(
    val id: String,
    val checklistItemId: String,
    val vehicleTypeId: String,
    val isMarkedForDeletion: Boolean = false,
    val internalObjectId: Int = 0
) 