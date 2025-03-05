package app.forku.data.repository.session

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.api.dto.session.EndSessionRequestDto
import app.forku.data.api.dto.vehicle.VehicleDto
import app.forku.data.api.dto.vehicle.VehicleStatusChangeRequestDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore,
    private val vehicleRepository: VehicleRepository
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

        try {
            // Update vehicle status first
            vehicleRepository.updateVehicleStatus(vehicleId, VehicleStatus.IN_USE)

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
                vehicleRepository.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE)
                throw Exception("Failed to create session: ${response.code()}")
            }
            
            return response.body()?.toDomain() 
                ?: throw Exception("No session data in response")
        } catch (e: Exception) {
            // Ensure vehicle status is rolled back on any error
            try {
                vehicleRepository.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE)
            } catch (rollbackError: Exception) {
                // Log rollback error but throw original error
                android.util.Log.e("Session", "Error rolling back vehicle status", rollbackError)
            }
            throw e
        }
    }

    override suspend fun endSession(sessionId: String): VehicleSession {
        val currentSession = getCurrentSession() 
            ?: throw Exception("No active session found")
            
        try {
            // Update vehicle status back to AVAILABLE
            vehicleRepository.updateVehicleStatus(currentSession.vehicleId, VehicleStatus.AVAILABLE)
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
                vehicleRepository.updateVehicleStatus(currentSession.vehicleId, VehicleStatus.IN_USE)
                throw Exception("Failed to end session: ${response.code()}")
            }
            
            return response.body()?.toDomain() 
                ?: throw Exception("Empty response when ending session")
        } catch (e: Exception) {
            // Ensure vehicle status is restored on any error
            vehicleRepository.updateVehicleStatus(currentSession.vehicleId, VehicleStatus.IN_USE)
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

    override suspend fun getOperatorSessionHistory(): List<VehicleSession> {
        val currentUser = authDataStore.getCurrentUser() 
            ?: throw Exception("User not authenticated")
        
        return try {
            val response = api.getSessions()
            if (response.isSuccessful) {
                val allSessions = response.body()?.map { it.toDomain() } ?: emptyList()
                // Filter sessions for current operator
                allSessions.filter { it.userId == currentUser.id }
            } else {
                android.util.Log.e("SessionRepo", "Error: ${response.code()} - ${response.message()}")
                throw Exception("Failed to get session history: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("SessionRepo", "Exception getting sessions", e)
            throw e
        }
    }
} 