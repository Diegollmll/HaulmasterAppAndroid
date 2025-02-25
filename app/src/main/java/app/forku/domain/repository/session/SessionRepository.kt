package app.forku.domain.repository.session

import app.forku.domain.model.session.VehicleSession

interface SessionRepository {
    suspend fun getCurrentSession(): VehicleSession?
    suspend fun startSession(vehicleId: String, checkId: String): VehicleSession
    suspend fun endSession(sessionId: String): VehicleSession
} 