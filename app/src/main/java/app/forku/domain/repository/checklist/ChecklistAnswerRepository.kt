package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistAnswer

interface ChecklistAnswerRepository {
    suspend fun getById(id: String): ChecklistAnswer?
    suspend fun getAll(): List<ChecklistAnswer>
    suspend fun getAllPaginated(page: Int, pageSize: Int): List<ChecklistAnswer>
    suspend fun save(item: ChecklistAnswer): ChecklistAnswer
    suspend fun delete(item: ChecklistAnswer)
    suspend fun getLastChecklistAnswerForVehicle(vehicleId: String): ChecklistAnswer?
    
    /**
     * ✅ NEW: Get checklist answers with explicit business and site filters (VIEW_FILTER mode)
     * Does NOT use user's personal context, uses provided filter parameters
     * Used for admin filtering across different sites
     */
    suspend fun getAllWithFilters(businessId: String, siteId: String?, page: Int = 1, pageSize: Int = 20): List<ChecklistAnswer>
} 