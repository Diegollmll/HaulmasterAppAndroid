package app.forku.data.api.dto.checklist


data class PerformChecklistRequestDto(
    val items: List<PerformChecklistItemRequestDto>,
    val startDateTime: String,
    val endDateTime: String? = null,
    val lastcheck_datetime: String,
    val status: String,
    val userId: String
)