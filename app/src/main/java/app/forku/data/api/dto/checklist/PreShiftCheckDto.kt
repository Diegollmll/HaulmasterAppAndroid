package app.forku.data.api.dto.checklist

data class PreShiftCheckDto(
    val id: String,
    val startDateTime: String,
    val endDateTime: String?,
    val lastcheck_datetime: String = java.time.Instant.now().toString(),
    val status: String,
    val userId: String,
    val vehicleId: String,
    val items: List<ChecklistItemDto>
) 