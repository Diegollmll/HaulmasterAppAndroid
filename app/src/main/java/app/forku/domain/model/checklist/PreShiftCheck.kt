package app.forku.domain.model.checklist

import app.forku.domain.model.checklist.ChecklistItem
import java.time.LocalDateTime

data class PreShiftCheck(
    val id: String,
    val vehicleId: String,
    val userId: String,
    val items: List<ChecklistItem>,
    val status: String,
    val startDateTime: String,
    val endDateTime: String? = null,
    val lastCheckDateTime: String? = null,
    val location: String? = null,
    val locationCoordinates: String? = null
) 