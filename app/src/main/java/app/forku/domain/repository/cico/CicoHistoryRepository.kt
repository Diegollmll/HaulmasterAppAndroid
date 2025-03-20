package app.forku.domain.repository.cico

import app.forku.domain.model.session.VehicleSession

interface CicoHistoryRepository {
    suspend fun getSessionsHistory(page: Int): List<VehicleSession>
    suspend fun getOperatorSessionsHistory(operatorId: String, page: Int): List<VehicleSession>
    suspend fun getCurrentUserSessionsHistory(page: Int): List<VehicleSession>
} 