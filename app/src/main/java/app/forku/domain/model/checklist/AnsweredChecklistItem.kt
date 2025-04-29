package app.forku.domain.model.checklist

data class AnsweredChecklistItem(
    val id: String,
    val checklistId: String,
    val question: String,
    val answer: String,
    val userId: String,
    val createdAt: String
) 