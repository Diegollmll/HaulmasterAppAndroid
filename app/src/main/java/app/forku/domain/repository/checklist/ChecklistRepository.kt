package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck

interface ChecklistRepository {
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck?
    suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?
    ): PreShiftCheck
} 