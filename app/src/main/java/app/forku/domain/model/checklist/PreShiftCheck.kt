package app.forku.domain.model.checklist

import app.forku.domain.model.checklist.ChecklistItem

data class PreShiftCheck(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val datetime: String,
    val status: String,
    val items: List<ChecklistItem>
) 