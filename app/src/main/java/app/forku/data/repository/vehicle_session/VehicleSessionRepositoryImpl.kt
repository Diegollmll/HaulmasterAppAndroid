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
import app.forku.core.business.BusinessContextManager
import app.forku.domain.model.session.AdminDashboardData
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.Vehicle
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
    private val locationManager: LocationManager,
    private val businessContextManager: BusinessContextManager
) : VehicleSessionRepository {
    override suspend fun getCurrentSession(): VehicleSession? {
        val currentUser = authDataStore.getCurrentUser() ?: return null
        try {
            // Use BusinessContextManager instead of currentUser.businessId
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("VehicleSessionRepo", "[getCurrentSession] Using businessId from BusinessContextManager: '$businessId'")
            
            val response = api.getAllSessions(businessId = businessId ?: "")
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

        // Use BusinessContextManager for consistent business and site context
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        android.util.Log.d("VehicleSessionRepo", "[startSession] Using businessId from BusinessContextManager: '$businessId', siteId: '$siteId'")

        // Get vehicle status using VehicleStatusRepository instead
        val vehicleStatus = vehicleStatusRepository.getVehicleStatus(
            vehicleId = vehicleId,
            businessId = businessId ?: ""
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
                businessId = businessId ?: "",
                siteId = siteId
            )
            
            // Use OffsetDateTime to avoid [America/Bogota] in the string
            val currentDateTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            android.util.Log.d("VehicleSessionRepo", "[startSession] currentDateTime (startTime) to send: $currentDateTime")

            // Get current location
            val locationState = locationManager.locationState.value
            val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                "${locationState.latitude},${locationState.longitude}"
            } else null
            
            // Create session with BusinessId and SiteId from BusinessContextManager
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
                notes = null,
                businessId = businessId, // Use BusinessContextManager business ID
                siteId = siteId // ✅ Use BusinessContextManager site ID
            )
            val dto = VehicleSessionMapper.toDto(newSession)
            val gson = Gson()
            val entityJson = gson.toJson(dto)
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            
            android.util.Log.d("VehicleSessionRepo", "[startSession] Saving session with businessId: '$businessId'")
            val response = api.saveSession(
                csrfToken, 
                cookie, 
                entityJson, 
                businessId = businessId // Use BusinessContextManager business ID
            )
            if (!response.isSuccessful) {
                // Rollback vehicle status if session creation fails
                vehicleStatusRepository.updateVehicleStatus(
                    vehicleId = vehicleId,
                    status = VehicleStatus.AVAILABLE,
                    businessId = businessId ?: "",
                    siteId = siteId
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
                businessId = businessId ?: "",
                siteId = siteId
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

        // Use BusinessContextManager for consistent business and site context
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        android.util.Log.d("VehicleSessionRepo", "[endSession] Using businessId from BusinessContextManager: '$businessId', siteId: '$siteId'")
            
        try {
            // Update vehicle status back to AVAILABLE
            vehicleStatusRepository.updateVehicleStatus(
                vehicleId = existingSession.vehicleId,
                status = VehicleStatus.AVAILABLE,
                businessId = businessId ?: "",
                siteId = siteId
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
            val response = api.saveSession(
                csrfToken, 
                cookie, 
                entityJson, 
                businessId = businessId
            )

            if (!response.isSuccessful) {
                // Rollback vehicle status if session update fails
                vehicleStatusRepository.updateVehicleStatus(
                    vehicleId = existingSession.vehicleId,
                    status = VehicleStatus.IN_USE,
                    businessId = businessId ?: "",
                    siteId = siteId
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
                businessId = businessId ?: "",
                siteId = siteId
            )
            throw e
        }
    }

    override suspend fun getActiveSessionForVehicle(vehicleId: String, businessId: String): VehicleSession? {
        android.util.Log.d("VehicleSession", "Fetching active session for vehicle: $vehicleId")
        return try {
            val response = api.getAllSessions(
                businessId = businessId
            )
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
        // Use BusinessContextManager instead of currentUser.businessId
        val businessId = businessContextManager.getCurrentBusinessId()
        android.util.Log.d("VehicleSessionRepo", "[getSessionsByUserId] Using businessId from BusinessContextManager: '$businessId'")
        
        return try {
            val response = api.getAllSessions(businessId = businessId ?: "")
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
        // Use BusinessContextManager instead of currentUser.businessId
        val businessId = businessContextManager.getCurrentBusinessId()
        android.util.Log.d("VehicleSessionRepo", "[getLastCompletedSessionForVehicle] Using businessId from BusinessContextManager: '$businessId'")
        
        return try {
            val response = api.getAllSessions(businessId = businessId ?: "")
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
        // Use BusinessContextManager instead of currentUser.businessId
        val businessId = businessContextManager.getCurrentBusinessId()
        android.util.Log.d("VehicleSessionRepo", "[getSessions] Using businessId from BusinessContextManager: '$businessId'")
        
        return try {
            android.util.Log.d("VehicleSessionRepo", "[getSessions] Llamando a getAllSessions con businessId: '$businessId'")
            
            val response = api.getAllSessions(businessId = businessId ?: "")
            android.util.Log.d("VehicleSessionRepo", "[getSessions] API response: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val sessions = response.body()!!
                android.util.Log.d("VehicleSessionRepo", "[getSessions] Total sesiones recibidas: ${sessions.size}")
                
                // Log first few sessions for debugging
                sessions.take(3).forEachIndexed { index, dto ->
                    android.util.Log.d("VehicleSessionRepo", "[getSessions]   Sesión $index: ID=${dto.Id}, Status=${dto.Status}, BusinessId=${dto.BusinessId}")
                }
                
                sessions.map { VehicleSessionMapper.toDomain(it) }
            } else {
                android.util.Log.w("VehicleSessionRepo", "[getSessions] Response not successful or body is null")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "[getSessions] Error getting all sessions", e)
            emptyList()
        }
    }

    // Nuevo método: obtener el conteo de vehículos en operación usando el endpoint optimizado
    override suspend fun getOperatingSessionsCount(businessId: String): Int {
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] === INICIANDO CONTEO EN REPOSITORY ===")
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] BusinessId recibido: '$businessId'")
        
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] CSRF Token obtenido: ${csrfToken.take(10)}...")
            
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] Cookie obtenido: ${cookie.take(20)}...")
            
            // ✅ FIX: Include SiteId in filter for multi-tenancy support
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] SiteId obtenido: '$siteId'")
            
            // Filter by Status, BusinessId and SiteId using Guid.Parse for GO Platform compatibility
            val filter = if (siteId != null && siteId.isNotBlank()) {
                "Status == 0 && BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "Status == 0 && BusinessId == Guid.Parse(\"$businessId\")"
            }
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] 🔍 Filtro construido: '$filter'")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] 🌐 Llamando a GET /dataset/api/vehiclesession/count?filter=$filter")
            
            val response = api.getOperatingSessionsCount(filter = filter, csrfToken = csrfToken, cookie = cookie)
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] 📡 Respuesta HTTP: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] 📊 Body: ${response.body()}")
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("VehicleSessionRepo", "[getOperatingSessionsCount] ❌ Error en respuesta: $errorBody")
                throw Exception("Failed to get operating sessions count: ${response.code()} - $errorBody")
            }
            
            val count = response.body() ?: 0
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] ✅ Conteo final: $count")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] === CONTEO COMPLETADO EN REPOSITORY ===")
            
            return count
            
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "[getOperatingSessionsCount] ❌ Excepción: ${e.message}", e)
            throw e
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

    // 🚀 OPTIMIZED: Get active sessions with all related data in one API call for AdminDashboard
    override suspend fun getActiveSessionsWithRelatedData(businessId: String): AdminDashboardData {
        android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] === 🚀 OPTIMIZED API CALL FOR ADMIN DASHBOARD ===")
        android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] BusinessId: '$businessId'")
        
        return try {
            // ✅ FIX: Include SiteId for consistent filtering
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] SiteId: '$siteId'")
            
            // Single API call with all related data included
            val include = "GOUser,GOUser.UserRoleItems,Vehicle,ChecklistAnswer"
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 🌐 API call with include: '$include'")
            
            val response = api.getAllSessions(
                businessId = businessId,
                include = include,
                filter = "Status == 0 && EndTime == null" // Only active sessions
            )
            
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 📡 Response: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("VehicleSessionRepo", "getAllSessions response received: ${response}")
                val sessionDtos = response.body()!!
                android.util.Log.d("VehicleSessionRepo", "getAllSessions received sessionDtos: ${sessionDtos}")

                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 📊 Total sessions received: ${sessionDtos.size}")
                
                // ✅ FIX: Filter by exact business and site match for consistency
                val filteredDtos = sessionDtos.filter { dto -> 
                    dto.Status == 0 && 
                    dto.EndTime == null &&
                    dto.BusinessId == businessId &&
                    (siteId == null || siteId.isBlank() || dto.siteId == siteId) // Include SiteId filter
                }
                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 🔍 Filtered active sessions for business '$businessId', site '$siteId': ${filteredDtos.size}")
                
                // Map sessions
                val activeSessions = filteredDtos.map { dto -> 
                    VehicleSessionMapper.toDomain(dto) 
                }
                
                // Extract unique vehicles from included data
                val vehicles = filteredDtos.mapNotNull { dto -> 
                    dto.Vehicle?.let { vehicleDto ->
                        vehicleDto.id?.let { id ->
                            id to vehicleDto.toDomain()
                        }
                    }
                }.toMap()
                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 🚗 Vehicles extracted: ${vehicles.size}")
                
                // Extract unique operators from included data
                val operators = filteredDtos.mapNotNull { dto -> 
                    dto.GOUser?.let { userDto ->
                        userDto.id?.let { id ->
                            id to userDto.toDomain()
                        }
                    }
                }.toMap()
                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 👥 Operators extracted: ${operators.size}")
                
                // Extract unique checklist answers from included data
                val checklistAnswers = filteredDtos.mapNotNull { dto -> 
                    dto.ChecklistAnswer?.let { checklistDto ->
                        checklistDto.id!! to checklistDto.toDomain()
                    }
                }.toMap()
                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] ✅ ChecklistAnswers extracted: ${checklistAnswers.size}")
                
                val result = AdminDashboardData(
                    activeSessions = activeSessions,
                    vehicles = vehicles,
                    operators = operators,
                    checklistAnswers = checklistAnswers
                )
                
                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] ✅ Final result - Sessions: ${result.activeSessions.size}, Vehicles: ${result.vehicles.size}, Operators: ${result.operators.size}, ChecklistAnswers: ${result.checklistAnswers.size}")
                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] === 🎉 OPTIMIZATION COMPLETE - 1 API CALL vs ${filteredDtos.size * 3 + 1} CALLS ===")
                
                return result
                
            } else {
                android.util.Log.w("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] ❌ Response unsuccessful or empty body")
                return AdminDashboardData(
                    activeSessions = emptyList(),
                    vehicles = emptyMap(),
                    operators = emptyMap(),
                    checklistAnswers = emptyMap()
                )
            }
            
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] ❌ Exception: ${e.message}", e)
            throw e
        }
    }
} 