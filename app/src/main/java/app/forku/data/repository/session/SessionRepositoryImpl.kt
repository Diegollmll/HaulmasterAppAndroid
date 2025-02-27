package app.forku.data.repository.session

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.EndSessionRequestDto
import app.forku.data.api.dto.vehicle.VehicleStatusChangeRequestDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.model.session.SessionStatus
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
        val currentUser = authDataStore.getCurrentUser() 
            ?: throw Exception("No user logged in")

        val checkResponse = api.getCheck(vehicleId, checkId)
        if (!checkResponse.isSuccessful) {
            throw Exception("Failed to get check: ${checkResponse.code()}")
        }

        val check = checkResponse.body() ?: throw Exception("Check not found")
        if (check.status != PreShiftStatus.COMPLETED_PASS.toString()) {
            throw Exception("Check is not approved. Current status: ${check.status}")
        }

        // Check if there's already an active session
        val currentSession = getCurrentSession()
        if (currentSession != null) {
            throw Exception("User already has an active session")
        }

        // Update vehicle status to IN_USE
        val statusUpdateResponse = api.updateVehicleStatus(
            vehicleId,
            VehicleStatusChangeRequestDto(status = "IN_USE")
        )
        
        if (!statusUpdateResponse.isSuccessful) {
            throw Exception("Failed to update vehicle status")
        }

        val currentDateTime = java.time.Instant.now().toString()
        val request = StartSessionRequestDto(
            vehicleId = vehicleId,
            checkId = checkId,
            timestamp = currentDateTime,
            startTime = currentDateTime,
            status = SessionStatus.ACTIVE.toString(),
            userId = currentUser.id
        )

        val response = api.createSession(vehicleId, request)
        if (!response.isSuccessful) {
            // Rollback vehicle status if session creation fails
            api.updateVehicleStatus(vehicleId, VehicleStatusChangeRequestDto(status = "AVAILABLE"))
            throw Exception("Failed to create session: ${response.code()}")
        }
        
        return response.body()?.toDomain() 
            ?: throw Exception("Empty response when starting session")
    }

    override suspend fun endSession(sessionId: String): VehicleSession {
        val currentSession = getCurrentSession() 
            ?: throw Exception("No active session found")
            
        try {
            // Update vehicle status back to AVAILABLE
            val statusUpdateResponse = api.updateVehicleStatus(
                currentSession.vehicleId,
                VehicleStatusChangeRequestDto(status = "AVAILABLE")
            )
            
            if (!statusUpdateResponse.isSuccessful) {
                throw Exception("Failed to update vehicle status")
            }
            
            val currentDateTime = java.time.Instant.now().toString()
            val request = EndSessionRequestDto(
                timestamp = currentDateTime,
                endTime = currentDateTime,
                status = SessionStatus.INACTIVE.toString(),
                notes = null
            )
            
            val response = api.updateSession(
                vehicleId = currentSession.vehicleId,
                sessionId = sessionId,
                request = request
            )

            if (!response.isSuccessful) {
                // Rollback vehicle status if session update fails
                api.updateVehicleStatus(
                    currentSession.vehicleId, 
                    VehicleStatusChangeRequestDto(status = "IN_USE")
                )
                throw Exception("Failed to end session: ${response.code()}")
            }
            
            return response.body()?.toDomain() 
                ?: throw Exception("Empty response when ending session")
        } catch (e: Exception) {
            // Ensure vehicle status is restored on any error
            api.updateVehicleStatus(
                currentSession.vehicleId,
                VehicleStatusChangeRequestDto(status = "IN_USE")
            )
            throw e
        }
    }

    override suspend fun getActiveSessionForVehicle(vehicleId: String): VehicleSession? {
        return try {
            val response = api.getVehicleSessions(vehicleId)
            if (!response.isSuccessful) return null
            
            response.body()?.find { session ->
                session.status.uppercase() == "ACTIVE"
            }?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
} 