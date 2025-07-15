package app.forku.domain.model.checklist

data class AnsweredChecklistItem(
    val id: String,
    val checklistId: String,
    val checklistVersion: String = "1.0",
    val checklistAnswerId: String,
    val checklistItemId: String,
    val checklistItemVersion: String = "1.0",
    val question: String,
    val answer: String,
    val userId: String,
    val createdAt: String,
    val isNew: Boolean = true,
    val isDirty: Boolean = true,
    val userComment: String? = null,
    val businessId: String? = null
) 