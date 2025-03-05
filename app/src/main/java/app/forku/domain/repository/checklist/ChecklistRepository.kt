package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.PreShiftStatus

interface ChecklistRepository {
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck?
    suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?,
        status: String = PreShiftStatus.IN_PROGRESS.toString()
    ): PreShiftCheck
} 