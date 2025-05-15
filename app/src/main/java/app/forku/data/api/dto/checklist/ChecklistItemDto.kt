package app.forku.data.api.dto.checklist

data class ChecklistItemDto(
    val `$type`: String = "ChecklistItemDataObject",
    val ChecklistId: String,
    val ChecklistItemCategoryId: String,
    val ChecklistItemSubcategoryId: String,
    val Description: String,
    val EnergySource: List<Int>,
    val ExpectedAnswer: Int,
    val Id: String,
    val IsCritical: Boolean,
    val Question: String,
    val RotationGroup: Int,
    val VehicleComponent: Int,
    val IsMarkedForDeletion: Boolean,
    val InternalObjectId: Int,
    val userAnswer: Int? = null
)