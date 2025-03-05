package app.forku.domain.model.checklist

import app.forku.domain.model.checklist.ChecklistItem
import java.time.LocalDateTime

data class PreShiftCheck(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDateTime: String,
    val endDateTime: String?,
    val lastCheckDateTime: String,
    val status: String,
    val items: List<ChecklistItem> = emptyList()
) 