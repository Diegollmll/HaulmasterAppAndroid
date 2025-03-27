package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus

interface ChecklistRepository {
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck?
    suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?,
        status: String = CheckStatus.IN_PROGRESS.toString()
    ): PreShiftCheck
    
    // New methods for global checks endpoint
    suspend fun getAllChecks(page: Int = 1): List<PreShiftCheck>
    suspend fun getCheckById(checkId: String): PreShiftCheck?
    suspend fun createGlobalCheck(check: PreShiftCheck): PreShiftCheck
    suspend fun updateGlobalCheck(checkId: String, check: PreShiftCheck): PreShiftCheck
    
    suspend fun hasChecklistInCreation(vehicleId: String): Boolean

    suspend fun canStartCheck(vehicleId: String): Boolean
} 
