package app.forku.data.repository.vehicle_session

import app.forku.data.api.VehicleSessionApi
import app.forku.data.mapper.VehicleSessionMapper
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.repository.session.SessionStatusChecker
import javax.inject.Inject

class VehicleSessionStatusCheckerImpl @Inject constructor(
    private val api: VehicleSessionApi
) : SessionStatusChecker {
    override suspend fun getActiveSessionForVehicle(vehicleId: String, businessId: String): VehicleSession? {
        return try {
            val response = api.getAllSessions(
                businessId = businessId
            )
            if (response.isSuccessful) {
                val sessions = response.body()?.mapNotNull { 
                    try {
                        VehicleSessionMapper.toDomain(it)
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