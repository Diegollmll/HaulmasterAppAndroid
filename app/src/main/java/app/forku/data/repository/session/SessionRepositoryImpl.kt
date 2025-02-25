package app.forku.data.repository.session

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.EndSessionRequestDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.repository.session.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore
) : SessionRepository {
    override suspend fun getCurrentSession(): VehicleSession? {
        return try {
            val currentUser = authDataStore.getCurrentUser() ?: return null
            val vehicles = api.getVehicles().body() ?: return null
            
            for (vehicle in vehicles) {
                try {
                    val response = api.getVehicleSessions(vehicle.id)
                    if (!response.isSuccessful) continue
                    
                    val activeSession = response.body()?.find { session ->
                        session.status.uppercase() == "ACTIVE" && 
                        session.userId == currentUser.id
                    }?.toDomain()
                    
                    if (activeSession != null) {
                        return activeSession
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun startSession(vehicleId: String, checkId: String): VehicleSession {
        val checkResponse = api.getCheck(vehicleId, checkId)
        if (!checkResponse.isSuccessful || checkResponse.body()?.status != "COMPLETED_PASS") {
            throw Exception("No valid check found or check not approved")
        }

        val request = StartSessionRequestDto(
            vehicleId = vehicleId,
            checkId = checkId,
            timestamp = java.time.Instant.now().toString(),
            status = "ACTIVE"
        )

        val response = api.createSession(vehicleId, request)
        return response.body()?.toDomain() 
            ?: throw Exception("Empty response when starting session")
    }

    override suspend fun endSession(sessionId: String): VehicleSession {
        val currentSession = getCurrentSession() 
            ?: throw Exception("No active session found")
            
        val request = EndSessionRequestDto(
            timestamp = java.time.Instant.now().toString(),
            notes = null
        )
        
        val response = api.updateSession(
            vehicleId = currentSession.vehicleId,
            sessionId = sessionId,
            request = request
        )
        
        return response.body()?.toDomain() 
            ?: throw Exception("Empty response when ending session")
    }
} 