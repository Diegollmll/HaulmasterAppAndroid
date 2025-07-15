package app.forku.domain.model.checklist

data class ChecklistChecklistItemCategory(
    val id: String,
    val checklistId: String,
    val checklistItemCategoryId: String,
    val isMarkedForDeletion: Boolean = false,
    val internalObjectId: Int = 0
) 