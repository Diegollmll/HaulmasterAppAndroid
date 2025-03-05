package app.forku.data.api.dto.checklist

data class UpdateChecklistRequestDto(
    val items: List<ChecklistItemDto>,
    val lastCheckDateTime: String,
    val endDateTime: String?,
    val status: String,
    val userId: String
)
