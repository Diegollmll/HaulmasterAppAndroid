package app.forku.data.repository.vehicle_session

import app.forku.data.api.GeneralApi
import app.forku.data.mapper.toDomain
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.repository.session.SessionStatusChecker
import javax.inject.Inject

class VehicleSessionStatusCheckerImpl @Inject constructor(
    private val api: GeneralApi
) : SessionStatusChecker {
    override suspend fun getActiveSessionForVehicle(vehicleId: String): VehicleSession? {
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.mapNotNull { 
                    try {
                        it.toDomain()
                    } catch (e: Exception) {
                        android.util.Log.e("SessionMapper", "Error parsing session: ${e.message}")
                        null
                    }
                } ?: emptyList()
                sessions.find { 
                    it.vehicleId == vehicleId && 
                    it.status == VehicleSessionStatus.OPERATING
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SessionChecker", "Error checking session status: ${e.message}")
            null
        }
    }
} 