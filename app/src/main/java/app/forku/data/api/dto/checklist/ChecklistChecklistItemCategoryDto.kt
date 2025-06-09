package app.forku.data.api.dto.checklist

data class ChecklistChecklistItemCategoryDto(
    val `$type`: String = "ChecklistChecklistItemCategoryDataObject",
    val ChecklistId: String,
    val ChecklistItemCategoryId: String,
    val Id: String,
    val IsMarkedForDeletion: Boolean = false,
    val InternalObjectId: Int
) 