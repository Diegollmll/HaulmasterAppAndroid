package app.forku.data.api.dto.checklist

data class AnsweredChecklistItemDto(
    val id: String = "",
    val checklistId: String = "",
    val question: String = "",
    val answer: String = "",
    val userId: String = "",
    val createdAt: String = ""
) 