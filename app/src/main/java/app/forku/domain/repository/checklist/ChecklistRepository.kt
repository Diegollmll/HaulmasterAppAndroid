package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus

interface ChecklistRepository {
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun getLastPreShiftCheck(vehicleId: String, businessId: String): PreShiftCheck?
    suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?,
        status: String = CheckStatus.IN_PROGRESS.toString(),
        locationCoordinates: String? = null
    ): PreShiftCheck
    
    // New methods for global checks endpoint
    suspend fun getAllChecks(page: Int = 1): List<PreShiftCheck>
    suspend fun getCheckById(checkId: String): PreShiftCheck?
    suspend fun createGlobalCheck(check: PreShiftCheck): PreShiftCheck
    suspend fun updateGlobalCheck(checkId: String, check: PreShiftCheck): PreShiftCheck
    
    suspend fun hasChecklistInCreation(vehicleId: String): Boolean

    suspend fun canStartCheck(vehicleId: String): Boolean
    
    // Admin methods for checklist management
    suspend fun getAllChecklists(businessId: String? = null): List<Checklist>
    suspend fun getChecklistsForManagement(businessId: String?): List<Checklist>
    suspend fun getChecklistById(id: String): Checklist?
    suspend fun createChecklist(checklist: Checklist): Checklist
    suspend fun updateChecklist(id: String, checklist: Checklist): Checklist
    suspend fun deleteChecklist(id: String): Boolean
} 
