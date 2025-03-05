package app.forku.data.api.dto.checklist


data class PerformChecklistRequestDto(
    val items: List<ChecklistItemDto>,
    val startDateTime: String,
    val lastCheckDateTime: String,
    val status: String,
    val userId: String,
)