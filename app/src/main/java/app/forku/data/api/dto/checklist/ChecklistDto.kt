package app.forku.data.api.dto.checklist

data class ChecklistDto(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val items: List<ChecklistItemDto> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
) 