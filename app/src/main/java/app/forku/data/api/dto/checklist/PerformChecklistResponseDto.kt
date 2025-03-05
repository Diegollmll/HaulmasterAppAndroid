package app.forku.data.api.dto.checklist

data class PerformChecklistResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val items: List<ChecklistItemDto>,
    val startDateTime: String,
    val endDateTime: String?,
    val lastCheckDateTime: String,
    val status: String
) 