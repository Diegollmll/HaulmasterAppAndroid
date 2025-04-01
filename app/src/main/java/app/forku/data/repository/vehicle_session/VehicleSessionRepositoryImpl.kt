package app.forku.data.repository.vehicle_session

import app.forku.data.api.GeneralApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionClosedMethod
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.core.location.LocationManager
import javax.inject.Inject

class VehicleSessionRepositoryImpl @Inject constructor(
    private val api: GeneralApi,
    private val authDataStore: AuthDataStore,
    private val vehicleStatusRepository: VehicleStatusRepository,
    private val checklistRepository: ChecklistRepository,
    private val locationManager: LocationManager
) : VehicleSessionRepository {
    override suspend fun getCurrentSession(): VehicleSession? {
        val userId = authDataStore.getCurrentUser()?.id ?: return null
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                sessions.find { 
                    it.userId == userId && 
                    it.status == VehicleSessionStatus.OPERATING
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
            
            val currentDateTime = java.time.Instant.now()
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

            // Get current location
            val locationState = locationManager.locationState.value
            val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                "${locationState.latitude},${locationState.longitude}"
            } else null
            
            // Create session
            val response = api.createSession(
                StartSessionRequestDto(
                    vehicleId = vehicleId,
                    checkId = checkId,
                    userId = currentUser.id,
                    startTime = currentDateTime,
                    timestamp = currentDateTime,
                    status = VehicleSessionStatus.OPERATING.toString(),
                    startLocationCoordinates = locationCoordinates
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

    override suspend fun endSession(
        sessionId: String, 
        closeMethod: VehicleSessionClosedMethod,
        adminId: String?,
        notes: String?
    ): VehicleSession {
        val sessionResponse = api.getSessionById(sessionId)
        if (!sessionResponse.isSuccessful) {
            throw Exception("Failed to get session details")
        }
            
        val existingSession = sessionResponse.body() ?: throw Exception("Session not found")
            
        // Determine who closed the session
        val currentUser = authDataStore.getCurrentUser()
        val closedBy = when (closeMethod) {
            VehicleSessionClosedMethod.USER_CLOSED -> currentUser?.id
            VehicleSessionClosedMethod.ADMIN_CLOSED -> adminId ?: currentUser?.id
            VehicleSessionClosedMethod.TIMEOUT_CLOSED -> "SYSTEM"
            VehicleSessionClosedMethod.GEOFENCE_CLOSED -> "SYSTEM"
        }

        // Verify permissions
        if (closeMethod == VehicleSessionClosedMethod.ADMIN_CLOSED && currentUser?.role != UserRole.ADMIN) {
            throw Exception("Only administrators can perform administrative session closure")
        }

        if (closeMethod == VehicleSessionClosedMethod.USER_CLOSED && currentUser?.id != existingSession.userId) {
            throw Exception("Users can only close their own sessions")
        }
            
        try {
            // Update vehicle status back to AVAILABLE
            vehicleStatusRepository.updateVehicleStatus(existingSession.vehicleId, VehicleStatus.AVAILABLE)
            val currentDateTime = java.time.Instant.now()
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)

            // Get current location for end coordinates
            val locationState = locationManager.locationState.value
            val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                "${locationState.latitude},${locationState.longitude}"
            } else null
            
            val updatedSession = existingSession.copy(
                endTime = currentDateTime,
                timestamp = currentDateTime,
                status = VehicleSessionStatus.NOT_OPERATING.toString(),
                closeMethod = closeMethod.name,
                closedBy = closedBy,
                notes = notes,
                endLocationCoordinates = locationCoordinates
            )
            
            val response = api.updateSession(
                sessionId = sessionId,
                session = updatedSession
            )

            if (!response.isSuccessful) {
                // Rollback vehicle status if session update fails
                vehicleStatusRepository.updateVehicleStatus(existingSession.vehicleId, VehicleStatus.IN_USE)
                throw Exception("Failed to end session: ${response.code()}")
            }
            
            return response.body()?.toDomain() 
                ?: throw Exception("Empty response when ending session")
        } catch (e: Exception) {
            // Ensure vehicle status is restored on any error
            vehicleStatusRepository.updateVehicleStatus(existingSession.vehicleId, VehicleStatus.IN_USE)
            throw e
        }
    }

    override suspend fun getActiveSessionForVehicle(vehicleId: String): VehicleSession? {
        android.util.Log.d("VehicleSession", "Fetching active session for vehicle: $vehicleId")
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                android.util.Log.d("VehicleSession", "API response successful. Status code: ${response.code()}")
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                android.util.Log.d("VehicleSession", "Total sessions fetched: ${sessions.size}")
                
                val activeSession = sessions.find { session ->
                    session.vehicleId == vehicleId && 
                    session.status == VehicleSessionStatus.OPERATING
                }
                
                android.util.Log.d("VehicleSession", "Found active session: $activeSession")
                activeSession?.let {
                    android.util.Log.d("VehicleSession", "Session details - Status: ${it.status}, EndTime: ${it.endTime}")
                }
                
                activeSession
            } else {
                android.util.Log.w("VehicleSession", "Failed to fetch sessions. Status code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSession", "Error getting active session: ${e.message}", e)
            null
        }
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

    override suspend fun getLastCompletedSessionForVehicle(vehicleId: String): VehicleSession? {
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                sessions
                    .filter { 
                        it.vehicleId == vehicleId && 
                        it.status == VehicleSessionStatus.NOT_OPERATING &&
                        it.endTime != null 
                    }
                    .maxByOrNull { it.endTime!! }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSession", "Error getting last completed session: ${e.message}")
            null
        }
    }

    override suspend fun getSessions(): List<VehicleSession> {
        return try {
            val response = api.getAllSessions()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "Error getting all sessions", e)
            emptyList()
        }
    }
} 