package app.forku.domain.repository.session

import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionClosedMethod
import app.forku.domain.model.session.AdminDashboardData

interface VehicleSessionRepository {
    suspend fun getCurrentSession(): VehicleSession?
    suspend fun startSession(
        vehicleId: String, 
        checkId: String, 
        initialHourMeter: String? = null
    ): VehicleSession
    suspend fun endSession(
        sessionId: String, 
        closeMethod: VehicleSessionClosedMethod = VehicleSessionClosedMethod.USER_CLOSED,
        adminId: String? = null,
        notes: String? = null,
        finalHourMeter: String? = null
    ): VehicleSession
    suspend fun getActiveSessionForVehicle(vehicleId: String, businessId: String): VehicleSession?
    suspend fun getOperatorSessionHistory(): List<VehicleSession>
    suspend fun getSessionsByUserId(userId: String): List<VehicleSession>
    suspend fun getLastCompletedSessionForVehicle(vehicleId: String): VehicleSession?
    suspend fun getSessions(): List<VehicleSession>
    suspend fun getOperatingSessionsCount(businessId: String, siteId: String? = null): Int
    suspend fun getSessionWithChecklistAnswer(sessionId: String): VehicleSession?
    
    // 🚀 OPTIMIZED: Get active sessions with all related data in one API call
    suspend fun getActiveSessionsWithRelatedData(businessId: String, siteId: String? = null): AdminDashboardData
} 