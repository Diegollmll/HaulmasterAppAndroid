package app.forku.data.api.dto.checklist

data class PreShiftCheckDto(
    val id: String,
    val startDateTime: String,
    val endDateTime: String?,
    val lastCheckDateTime: String,
    val status: String,
    val userId: String,
    val vehicleId: String,
    val items: List<ChecklistItemDto>,
    val locationCoordinates: String? = null
) 