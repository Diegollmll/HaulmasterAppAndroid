package app.forku.domain.repository.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionClosedMethod

interface VehicleSessionRepository {
    suspend fun getCurrentSession(): VehicleSession?
    suspend fun startSession(vehicleId: String, checkId: String): VehicleSession
    suspend fun endSession(
        sessionId: String, 
        closeMethod: VehicleSessionClosedMethod = VehicleSessionClosedMethod.USER_CLOSED,
        adminId: String? = null,
        notes: String? = null
    ): VehicleSession
    suspend fun getActiveSessionForVehicle(vehicleId: String, businessId: String): VehicleSession?
    suspend fun getOperatorSessionHistory(): List<VehicleSession>
    suspend fun getSessionsByUserId(userId: String): List<VehicleSession>
    suspend fun getLastCompletedSessionForVehicle(vehicleId: String): VehicleSession?
    suspend fun getSessions(): List<VehicleSession>
    suspend fun getOperatingSessionsCount(businessId: String): Int
    suspend fun getSessionWithChecklistAnswer(sessionId: String): VehicleSession?
} 