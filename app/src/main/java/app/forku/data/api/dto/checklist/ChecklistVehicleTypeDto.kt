package app.forku.data.api.dto.checklist

data class ChecklistVehicleTypeDto(
    val `$type`: String = "ChecklistVehicleTypeDataObject",
    val ChecklistId: String,
    val Id: String,
    val VehicleTypeId: String,
    val IsMarkedForDeletion: Boolean = false,
    val InternalObjectId: Int
) 