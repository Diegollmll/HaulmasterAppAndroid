package app.forku.data.api.dto.checklist

data class UpdateChecklistRequestDto(
    val items: List<PerformChecklistItemRequestDto>,
    val endDateTime: String,
    val lastcheck_datetime: String,
    val status: String,
    val userId: String
) 