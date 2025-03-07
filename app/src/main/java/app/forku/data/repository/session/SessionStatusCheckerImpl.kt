package app.forku.data.repository.session

import app.forku.data.api.Sub7Api
import app.forku.data.mapper.toDomain
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.repository.session.SessionStatusChecker
import javax.inject.Inject

class SessionStatusCheckerImpl @Inject constructor(
    private val api: Sub7Api
) : SessionStatusChecker {
    override suspend fun getActiveSessionForVehicle(vehicleId: String): VehicleSession? {
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                sessions.find { 
                    it.vehicleId == vehicleId && 
                    it.status == SessionStatus.ACTIVE 
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 