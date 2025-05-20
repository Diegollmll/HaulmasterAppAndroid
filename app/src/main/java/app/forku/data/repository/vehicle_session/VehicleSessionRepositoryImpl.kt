package app.forku.data.repository.vehicle_session

import app.forku.data.api.VehicleSessionApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.dto.session.StartSessionRequestDto
import app.forku.data.mapper.VehicleSessionMapper
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionClosedMethod
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.core.location.LocationManager
import javax.inject.Inject
import com.google.gson.Gson
import app.forku.core.Constants
import java.util.UUID
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class VehicleSessionRepositoryImpl @Inject constructor(
    private val api: VehicleSessionApi,
    private val authDataStore: AuthDataStore,
    private val vehicleStatusRepository: VehicleStatusRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val locationManager: LocationManager
) : VehicleSessionRepository {
    override suspend fun getCurrentSession(): VehicleSession? {
        val currentUser = authDataStore.getCurrentUser() ?: return null
        try {
            val response = api.getAllSessions(currentUser.businessId ?: Constants.BUSINESS_ID)
            if (response.isSuccessful) {
                val sessions = response.body()?.let { dtos ->
                    dtos.map { dto -> VehicleSessionMapper.toDomain(dto) }
                } ?: emptyList()
                
                return sessions.find { 
                    it.userId == currentUser.id && 
                    it.status == VehicleSessionStatus.OPERATING
                }
            }
            return null
        } catch (e: Exception) {
            android.util.Log.e("VehicleSession", "Error getting current session: ${e.message}", e)
            return null
        }
    }

    override suspend fun startSession(vehicleId: String, checkId: String): VehicleSession {
        val currentUser = authDataStore.getCurrentUser()
            ?: throw Exception("No user logged in")

        // Get vehicle status using VehicleStatusRepository instead
        val vehicleStatus = vehicleStatusRepository.getVehicleStatus(
            vehicleId = vehicleId,
            businessId = currentUser.businessId ?: Constants.BUSINESS_ID
        )

        if (!vehicleStatus.isAvailable()) {
            throw Exception(vehicleStatus.getErrorMessage())
        }

        // Get checklist answer using ChecklistAnswerRepository
        val checklistAnswer = checklistAnswerRepository.getById(checkId)
            ?: throw Exception("ChecklistAnswer not found")
        if (checklistAnswer.status != CheckStatus.COMPLETED_PASS.ordinal) {
            throw Exception("ChecklistAnswer is not approved. Current status: ${checklistAnswer.status}")
        }

        // Check if there's already an active session
        val currentSession = getCurrentSession()
        if (currentSession != null) {
            throw Exception("User already has an active session")
        }

        try {
            // Update vehicle status first
            vehicleStatusRepository.updateVehicleStatus(
                vehicleId = vehicleId,
                status = VehicleStatus.IN_USE,
                businessId = currentUser.businessId ?: Constants.BUSINESS_ID
            )
            
            // Use OffsetDateTime to avoid [America/Bogota] in the string
            val currentDateTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            android.util.Log.d("VehicleSessionRepo", "[startSession] currentDateTime (startTime) to send: $currentDateTime")

            // Get current location
            val locationState = locationManager.locationState.value
            val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                "${locationState.latitude},${locationState.longitude}"
            } else null
            
            // Create session
            val sessionId = UUID.randomUUID().toString()
            val newSession = VehicleSession(
                id = sessionId, // Always a valid GUID for new sessions
                vehicleId = vehicleId,
                userId = currentUser.id,
                checkId = checkId,
                startTime = currentDateTime,
                endTime = null,
                status = VehicleSessionStatus.OPERATING,
                startLocationCoordinates = locationCoordinates,
                endLocationCoordinates = null,
                durationMinutes = null,
                timestamp = currentDateTime,
                closeMethod = null,
                closedBy = null,
                notes = null
            )
            val dto = VehicleSessionMapper.toDto(newSession)
            val gson = Gson()
            val entityJson = gson.toJson(dto)
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val response = api.saveSession(csrfToken, cookie, entityJson)
            if (!response.isSuccessful) {
                // Rollback vehicle status if session creation fails
                vehicleStatusRepository.updateVehicleStatus(
                    vehicleId = vehicleId,
                    status = VehicleStatus.AVAILABLE,
                    businessId = currentUser.businessId ?: Constants.BUSINESS_ID
                )
                throw Exception("Failed to create session: ${response.code()}")
            }
            val createdSession = response.body()?.let { VehicleSessionMapper.toDomain(it) }
            android.util.Log.d("VehicleSessionRepo", "[startSession] startTime received from backend: ${createdSession?.startTime}")
            return createdSession ?: throw Exception("Failed to start session: Empty response")
        } catch (e: Exception) {
            // Revert vehicle status on failure
            vehicleStatusRepository.updateVehicleStatus(
                vehicleId = vehicleId,
                status = VehicleStatus.AVAILABLE,
                businessId = currentUser.businessId ?: Constants.BUSINESS_ID
            )
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
            
        val existingSession = sessionResponse.body()?.let { VehicleSessionMapper.toDomain(it) }
            ?: throw Exception("Session not found")
            
        // Determine who closed the session
        val currentUser = authDataStore.getCurrentUser()
            ?: throw Exception("No user logged in")
            
        val closedBy = when (closeMethod) {
            VehicleSessionClosedMethod.USER_CLOSED -> currentUser.id
            VehicleSessionClosedMethod.ADMIN_CLOSED -> adminId ?: currentUser.id
            VehicleSessionClosedMethod.TIMEOUT_CLOSED -> "SYSTEM"
            VehicleSessionClosedMethod.GEOFENCE_CLOSED -> "SYSTEM"
        }

        // Verify permissions
        if (closeMethod == VehicleSessionClosedMethod.ADMIN_CLOSED && currentUser.role != UserRole.ADMIN) {
            throw Exception("Only administrators can perform administrative session closure")
        }

        if (closeMethod == VehicleSessionClosedMethod.USER_CLOSED && currentUser.id != existingSession.userId) {
            throw Exception("Users can only close their own sessions")
        }
            
        try {
            // Update vehicle status back to AVAILABLE
            vehicleStatusRepository.updateVehicleStatus(
                vehicleId = existingSession.vehicleId,
                status = VehicleStatus.AVAILABLE,
                businessId = currentUser.businessId ?: Constants.BUSINESS_ID
            )
            
            // Use OffsetDateTime to avoid [America/Bogota] in the string
            val currentDateTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            // Get current location for end coordinates
            val locationState = locationManager.locationState.value
            val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                "${locationState.latitude},${locationState.longitude}"
            } else null
            
            // Calcular duración en minutos
            val start = java.time.ZonedDateTime.parse(existingSession.startTime).toInstant()
            val end = java.time.ZonedDateTime.parse(currentDateTime).toInstant()
            val duration = java.time.Duration.between(start, end).toMinutes().toInt()
            
            val updatedSession = existingSession.copy(
                endTime = currentDateTime,
                timestamp = currentDateTime,
                status = VehicleSessionStatus.NOT_OPERATING,
                closeMethod = closeMethod,
                closedBy = closedBy,
                notes = notes,
                endLocationCoordinates = locationCoordinates,
                durationMinutes = duration
            )
            
            // Antes de enviar el DTO para cerrar sesión, asegúrate de que IsNew=false
            val dto = VehicleSessionMapper.toDto(updatedSession).copy(IsNew = false)
            val gson = Gson()
            val entityJson = gson.toJson(dto)
            android.util.Log.d("VehicleSessionRepo", "[endSession] Payload JSON enviado a la API: $entityJson")
            android.util.Log.d("VehicleSessionRepo", "[endSession] Campos clave: Id=${dto.Id}, Status=${dto.Status}, EndTime=${dto.EndTime}, VehicleSessionClosedMethod=${dto.VehicleSessionClosedMethod}, ClosedBy=${dto.ClosedBy}, IsNew=${dto.IsNew}")
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            val response = api.saveSession(csrfToken, cookie, entityJson)

            if (!response.isSuccessful) {
                // Rollback vehicle status if session update fails
                vehicleStatusRepository.updateVehicleStatus(
                    vehicleId = existingSession.vehicleId,
                    status = VehicleStatus.IN_USE,
                    businessId = currentUser.businessId ?: Constants.BUSINESS_ID
                )
                throw Exception("Failed to end session: ${response.code()}")
            }
            
            return response.body()?.let { VehicleSessionMapper.toDomain(it) }
                ?: throw Exception("Empty response when ending session")
        } catch (e: Exception) {
            // Ensure vehicle status is restored on any error
            vehicleStatusRepository.updateVehicleStatus(
                vehicleId = existingSession.vehicleId,
                status = VehicleStatus.IN_USE,
                businessId = currentUser.businessId ?: Constants.BUSINESS_ID
            )
            throw e
        }
    }

    override suspend fun getActiveSessionForVehicle(vehicleId: String, businessId: String): VehicleSession? {
        android.util.Log.d("VehicleSession", "Fetching active session for vehicle: $vehicleId")
        return try {
            val response = api.getAllSessions(businessId)
            if (response.isSuccessful) {
                android.util.Log.d("VehicleSession", "API response successful. Status code: ${response.code()}")
                val sessions = response.body()?.map { VehicleSessionMapper.toDomain(it) } ?: emptyList()
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
        val currentUser = authDataStore.getCurrentUser() ?: return emptyList()
        return try {
            val response = api.getAllSessions(currentUser.businessId ?: Constants.BUSINESS_ID)
            if (response.isSuccessful) {
                val sessions = response.body()?.map { VehicleSessionMapper.toDomain(it) } ?: emptyList()
                sessions.filter { it.userId == userId }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getLastCompletedSessionForVehicle(vehicleId: String): VehicleSession? {
        val currentUser = authDataStore.getCurrentUser() ?: return null
        return try {
            val response = api.getAllSessions(currentUser.businessId ?: Constants.BUSINESS_ID)
            if (response.isSuccessful) {
                val sessions = response.body()?.map { VehicleSessionMapper.toDomain(it) } ?: emptyList()
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
        val currentUser = authDataStore.getCurrentUser() ?: return emptyList()
        return try {
            val response = api.getAllSessions(currentUser.businessId ?: Constants.BUSINESS_ID)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { VehicleSessionMapper.toDomain(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "Error getting all sessions", e)
            emptyList()
        }
    }

    // Nuevo método: obtener el conteo de vehículos en operación usando el endpoint optimizado
    override suspend fun getOperatingSessionsCount(businessId: String): Int {
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] A:")
        val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] B:")
        val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] C:")
        // BusinessId For future use "&& BusinessId == \"$businessId\""
        val filter = "Status == 0" 
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] Filtro usado: $filter")
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] Llamando a /dataset/api/vehiclesession/count ...")
        val response = api.getOperatingSessionsCount(filter = filter, csrfToken = csrfToken, cookie = cookie)
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] Respuesta: isSuccessful=${response.isSuccessful}, code=${response.code()}, body=${response.body()}")
        if (response.isSuccessful) {
            return response.body() ?: 0
        } else {
            throw Exception("Failed to get operating sessions count: ${response.code()}")
        }
    }

    override suspend fun getSessionWithChecklistAnswer(sessionId: String): VehicleSession? {
        return try {
            val response = api.getSessionById(sessionId, include = "ChecklistAnswer")
            if (response.isSuccessful) {
                response.body()?.let { VehicleSessionMapper.toDomain(it) }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "Error fetching session with ChecklistAnswer: ${e.message}", e)
            null
        }
    }
} 