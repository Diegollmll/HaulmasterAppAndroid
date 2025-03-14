package app.forku.data.repository.vehicle_session

import app.forku.data.api.GeneralApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import javax.inject.Inject

class VehicleSessionRepositoryImpl @Inject constructor(
    private val api: GeneralApi,
    private val authDataStore: AuthDataStore,
    private val vehicleStatusRepository: VehicleStatusRepository,
    private val checklistRepository: ChecklistRepository
) : SessionRepository {
    override suspend fun getCurrentSession(): VehicleSession? {
        val userId = authDataStore.getCurrentUser()?.id ?: return null
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                sessions.find { 
                    it.userId == userId && 
                    it.status == SessionStatus.ACTIVE 
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun startSession(vehicleId: String, checkId: String): VehicleSession {
        val currentUser = authDataStore.getCurrentUser()
            ?: throw Exception("No user logged in")

        // Get vehicle status using VehicleStatusRepository instead
        val vehicleStatus = vehicleStatusRepository.getVehicleStatus(vehicleId)

        if (!vehicleStatus.isAvailable()) {
            throw Exception(vehicleStatus.getErrorMessage())
        }

        // Get check using the new global endpoint
        val check = checklistRepository.getCheckById(checkId) 
            ?: throw Exception("Check not found")
            
        if (check.status != CheckStatus.COMPLETED_PASS.toString()) {
            throw Exception("Check is not approved. Current status: ${check.status}")
        }

        // Check if there's already an active session
        val currentSession = getCurrentSession()
        if (currentSession != null) {
            throw Exception("User already has an active session")
        }

        try {
            // Update vehicle status first
            vehicleStatusRepository.updateVehicleStatus(vehicleId, VehicleStatus.IN_USE)
            
            val currentDateTime = java.time.Instant.now().toString()
            
            // Create session
            val response = api.createSession(
                StartSessionRequestDto(
                    vehicleId = vehicleId,
                    checkId = checkId,
                    userId = currentUser.id,
                    startTime = currentDateTime,
                    timestamp = currentDateTime,
                    status = SessionStatus.ACTIVE.toString()
                )
            )

            if (!response.isSuccessful) {
                // Rollback vehicle status if session creation fails
                vehicleStatusRepository.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE)
                throw Exception("Failed to create session: ${response.code()}")
            }

            return response.body()?.toDomain() 
                ?: throw Exception("Failed to start session: Empty response")
                
        } catch (e: Exception) {
            // Revert vehicle status on failure
            vehicleStatusRepository.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE)
            throw e
        }
    }

    override suspend fun endSession(sessionId: String): VehicleSession {
        val currentSession = getCurrentSession() 
            ?: throw Exception("No active session found")
            
        try {
            // Update vehicle status back to AVAILABLE
            vehicleStatusRepository.updateVehicleStatus(currentSession.vehicleId, VehicleStatus.AVAILABLE)
            val currentDateTime = java.time.Instant.now().toString()
            
            // Get the current session and update its fields
            val sessionResponse = api.getSessionById(sessionId)
            if (!sessionResponse.isSuccessful) {
                throw Exception("Failed to get session details")
            }
            
            val existingSession = sessionResponse.body() ?: throw Exception("Session not found")
            val updatedSession = existingSession.copy(
                endTime = currentDateTime,
                timestamp = currentDateTime,
                status = SessionStatus.INACTIVE.toString()
            )
            
            val response = api.updateSession(
                sessionId = sessionId,
                session = updatedSession
            )

            if (!response.isSuccessful) {
                // Rollback vehicle status if session update fails
                vehicleStatusRepository.updateVehicleStatus(currentSession.vehicleId, VehicleStatus.IN_USE)
                throw Exception("Failed to end session: ${response.code()}")
            }
            
            return response.body()?.toDomain() 
                ?: throw Exception("Empty response when ending session")
        } catch (e: Exception) {
            // Ensure vehicle status is restored on any error
            vehicleStatusRepository.updateVehicleStatus(currentSession.vehicleId, VehicleStatus.IN_USE)
            throw e
        }
    }

    override suspend fun getActiveSessionForVehicle(vehicleId: String): VehicleSession? {
        android.util.Log.d("VehicleSession", "Fetching active session for vehicle: $vehicleId")
        val response = api.getAllSessions()
        if (response.isSuccessful) {
            android.util.Log.d("VehicleSession", "API response successful. Status code: ${response.code()}")
            val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
            android.util.Log.d("VehicleSession", "Total sessions fetched: ${sessions.size}")
            
            val activeSession = sessions.find { 
                it.vehicleId == vehicleId && 
                it.status == SessionStatus.ACTIVE 
            }
            
            android.util.Log.d("VehicleSession", "Active session for vehicle $vehicleId: $activeSession")
            return activeSession
        }
        android.util.Log.w("VehicleSession", "Failed to fetch sessions. Status code: ${response.code()}")
        return null
    }

    override suspend fun getOperatorSessionHistory(): List<VehicleSession> {
        val userId = authDataStore.getCurrentUser()?.id ?: return emptyList()
        return getSessionsByUserId(userId)
    }

    override suspend fun getSessionsByUserId(userId: String): List<VehicleSession> {
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                sessions.filter { it.userId == userId }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 