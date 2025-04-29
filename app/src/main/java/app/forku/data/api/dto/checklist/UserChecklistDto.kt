package app.forku.data.api.dto.checklist

data class UserChecklistDto(
    val id: String = "",
    val userId: String = "",
    val checklistId: String = "",
    val completedAt: String? = null
) 