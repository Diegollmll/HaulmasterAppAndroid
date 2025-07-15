package app.forku.domain.repository.cico

import app.forku.domain.model.session.VehicleSession

interface CicoHistoryRepository {
    suspend fun getSessionsHistory(page: Int): List<VehicleSession>
    suspend fun getOperatorSessionsHistory(operatorId: String, page: Int): List<VehicleSession>
    suspend fun getCurrentUserSessionsHistory(page: Int): List<VehicleSession>
    
    // ✅ New methods with business and site filtering for admin
    suspend fun getSessionsHistoryWithFilters(
        page: Int,
        businessId: String? = null,
        siteId: String? = null,
        operatorId: String? = null
    ): List<VehicleSession>
} 