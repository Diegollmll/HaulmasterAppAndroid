package app.forku.data.api.dto.checklist

data class PerformChecklistResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val items: List<PerformChecklistItemRequestDto>,
    val startDateTime: String,
    val endDateTime: String?,
    val lastcheck_datetime: String,
    val status: String
) 