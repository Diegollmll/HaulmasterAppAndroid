package app.forku.domain.model.checklist

data class AnsweredChecklistItem(
    val id: String,
    val checklistId: String,
    val checklistAnswerId: String,
    val checklistItemId: String,
    val question: String,
    val answer: String,
    val userId: String,
    val createdAt: String,
    val isNew: Boolean = true,
    val isDirty: Boolean = true,
    val userComment: String? = null
) 