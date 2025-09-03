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
import app.forku.domain.repository.vehicle.VehicleRepository
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
    private val vehicleRepository: VehicleRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val locationManager: LocationManager,
    private val businessContextManager: BusinessContextManager,
    private val validateUserCertificationUseCase: app.forku.domain.usecase.certification.ValidateUserCertificationUseCase
) : VehicleSessionRepository {
    override suspend fun getCurrentSession(): VehicleSession? {
        val currentUser = authDataStore.getCurrentUser() ?: return null
        try {
            // Use BusinessContextManager instead of currentUser.businessId
            val businessId = businessContextManager.getCurrentBusinessId()
            android.util.Log.d("VehicleSessionRepo", "[getCurrentSession] Using businessId from BusinessContextManager: '$businessId'")
            
            val response = api.getAllSessions(businessId = businessId ?: "", include = "Vehicle,GOUser")
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

    override suspend fun startSession(
        vehicleId: String, 
        checkId: String, 
        initialHourMeter: String?
    ): VehicleSession {
        android.util.Log.d("VehicleSessionRepo", "🚀 [startSession] === INICIANDO CREACIÓN DE SESIÓN ===")
        android.util.Log.d("VehicleSessionRepo", "🚀 [startSession] Parámetros recibidos:")
        android.util.Log.d("VehicleSessionRepo", "  - vehicleId: '$vehicleId'")
        android.util.Log.d("VehicleSessionRepo", "  - checkId: '$checkId'")
        android.util.Log.d("VehicleSessionRepo", "  - initialHourMeter: '$initialHourMeter'")



        val currentUser = authDataStore.getCurrentUser()
        android.util.Log.d("VehicleSessionRepo", "👤 [startSession] Usuario actual obtenido: ${currentUser?.fullName ?: "NULL"}")
        if (currentUser == null) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] No hay usuario logueado")
            throw Exception("No user logged in")
        }

        // Use BusinessContextManager for consistent business and site context
        android.util.Log.d("VehicleSessionRepo", "🏢 [startSession] Obteniendo contexto de negocio...")
        val businessId = businessContextManager.getCurrentBusinessId()
        android.util.Log.d("VehicleSessionRepo", "🏢 [startSession] BusinessId obtenido: '$businessId'")
        
        val siteId = businessContextManager.getCurrentSiteId()
        android.util.Log.d("VehicleSessionRepo", "🏢 [startSession] SiteId obtenido: '$siteId'")
        
        android.util.Log.d("VehicleSessionRepo", "🚗 [startSession] Obteniendo vehículo con businessId: '$businessId'")
        val vehicle = vehicleRepository.getVehicle(vehicleId, businessId ?: "")
        android.util.Log.d("VehicleSessionRepo", "🚗 [startSession] Vehículo obtenido: ${vehicle?.codename ?: "NULL"}")
        android.util.Log.d("VehicleSessionRepo", "🚗 [startSession] Vehículo photoModel: ${vehicle?.photoModel ?: "NULL"}")
        
        android.util.Log.d("VehicleSessionRepo", "[startSession] Using businessId from BusinessContextManager: '$businessId', siteId: '$siteId'")

        // ✅ VALIDATE USER CERTIFICATIONS FOR THIS VEHICLE TYPE
        android.util.Log.d("VehicleSessionRepo", "🔐 [startSession] === VALIDANDO CERTIFICACIONES ===")
        try {
            android.util.Log.d("VehicleSessionRepo", "🔐 [startSession] Validando certificación para usuario: ${currentUser.id}")
            android.util.Log.d("VehicleSessionRepo", "🔐 [startSession] Validando certificación para vehículo: $vehicleId")
            val validationResult = validateUserCertificationUseCase(currentUser.id, vehicleId)
            android.util.Log.d("VehicleSessionRepo", "🔐 [startSession] Resultado de validación: ${validationResult.isValid}")
            android.util.Log.d("VehicleSessionRepo", "🔐 [startSession] Mensaje de validación: ${validationResult.message}")
            
            if (!validationResult.isValid) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Validación de certificación falló: ${validationResult.message}")
                throw Exception("Certification Required: ${validationResult.message}")
            } else {
                android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Validación de certificación exitosa: ${validationResult.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Error validando certificación: ${e.message}")
            throw Exception("Certification validation failed: ${e.message}")
        }



        // ✅ VALIDATION: Validate initial hour meter if provided
        android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] === VALIDANDO HOUR METER INICIAL ===")
        if (!initialHourMeter.isNullOrBlank()) {
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Hour meter inicial proporcionado: '$initialHourMeter'")
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Hour meter actual del vehículo: '${vehicle.currentHourMeter}'")
            
            val validationResult = app.forku.core.validation.HourMeterValidator.validateInitialHourMeter(
                initialValue = initialHourMeter,
                vehicleCurrentValue = vehicle.currentHourMeter
            )
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Resultado de validación: ${validationResult.isValid}")
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Mensaje de validación: ${validationResult.errorMessage ?: "OK"}")
            
            if (!validationResult.isValid) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Validación de hour meter inicial falló: ${validationResult.errorMessage}")
                throw Exception("Invalid initial hour meter: ${validationResult.errorMessage}")
            }
            
            android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Validación de hour meter inicial exitosa: ${validationResult.validatedValue}")
        } else {
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] No se proporcionó hour meter inicial - saltando validación")
        }

        // Get vehicle status using VehicleStatusRepository instead
        android.util.Log.d("VehicleSessionRepo", "📊 [startSession] === VERIFICANDO ESTADO DEL VEHÍCULO ===")
        android.util.Log.d("VehicleSessionRepo", "📊 [startSession] Obteniendo estado del vehículo: $vehicleId")
        val vehicleStatus = vehicleStatusRepository.getVehicleStatus(
            vehicleId = vehicleId,
            businessId = businessId ?: ""
        )
        android.util.Log.d("VehicleSessionRepo", "📊 [startSession] Estado del vehículo obtenido: ${vehicleStatus.name}")
        android.util.Log.d("VehicleSessionRepo", "📊 [startSession] ¿Está disponible?: ${vehicleStatus.isAvailable()}")

        if (!vehicleStatus.isAvailable()) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Vehículo no está disponible: ${vehicleStatus.getErrorMessage()}")
            throw Exception(vehicleStatus.getErrorMessage())
        }
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Vehículo está disponible para uso")

        // Get checklist answer using ChecklistAnswerRepository
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] === VERIFICANDO CHECKLIST ===")
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Obteniendo checklist answer con ID: $checkId")
        val checklistAnswer = checklistAnswerRepository.getById(checkId)
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Checklist answer encontrado: ${checklistAnswer != null}")
        if (checklistAnswer == null) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] ChecklistAnswer no encontrado")
            throw Exception("ChecklistAnswer not found")
        }
        
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Estado del checklist: ${checklistAnswer.status}")
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Estado requerido: ${CheckStatus.COMPLETED_PASS.ordinal}")
        
        if (checklistAnswer.status != CheckStatus.COMPLETED_PASS.ordinal) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Checklist no está aprobado. Estado actual: ${checklistAnswer.status}")
            throw Exception("ChecklistAnswer is not approved. Current status: ${checklistAnswer.status}")
        }
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Checklist está aprobado y listo para usar")

        // Check if there's already an active session
        android.util.Log.d("VehicleSessionRepo", "🔍 [startSession] === VERIFICANDO SESIONES ACTIVAS ===")
        android.util.Log.d("VehicleSessionRepo", "🔍 [startSession] Verificando si el usuario ya tiene una sesión activa")
        val currentSession = getCurrentSession()
        android.util.Log.d("VehicleSessionRepo", "🔍 [startSession] Sesión activa encontrada: ${currentSession != null}")
        if (currentSession != null) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Usuario ya tiene una sesión activa: ${currentSession.id}")
            throw Exception("User already has an active session")
        }
        android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Usuario no tiene sesiones activas - puede crear una nueva")

        try {
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] === ACTUALIZANDO ESTADO DEL VEHÍCULO ===")
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Cambiando estado del vehículo a IN_USE")
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Parámetros para actualización:")
            android.util.Log.d("VehicleSessionRepo", "  - vehicleId: $vehicleId")
            android.util.Log.d("VehicleSessionRepo", "  - status: IN_USE")
            android.util.Log.d("VehicleSessionRepo", "  - businessId: '$businessId'")
            android.util.Log.d("VehicleSessionRepo", "  - siteId: '$siteId'")
            
            // Update vehicle status first
            val updateResult = vehicleStatusRepository.updateVehicleStatus(
                vehicleId = vehicleId,
                status = VehicleStatus.IN_USE,
                businessId = businessId ?: "",
                siteId = siteId
            )
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Resultado de actualización del estado: $updateResult")
            if (!updateResult) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Falló la actualización del estado del vehículo")
                throw Exception("Failed to update vehicle status")
            }
            android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Estado del vehículo actualizado exitosamente a IN_USE")
            
            // Use OffsetDateTime to avoid [America/Bogota] in the string
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] === PREPARANDO DATOS DE LA SESIÓN ===")
            val currentDateTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] DateTime actual (startTime): $currentDateTime")

            // Get current location
            android.util.Log.d("VehicleSessionRepo", "📍 [startSession] Obteniendo ubicación actual...")
            val locationState = locationManager.locationState.value
            android.util.Log.d("VehicleSessionRepo", "📍 [startSession] Estado de ubicación: lat=${locationState.latitude}, lng=${locationState.longitude}")
            val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                "${locationState.latitude},${locationState.longitude}"
            } else null
            android.util.Log.d("VehicleSessionRepo", "📍 [startSession] Coordenadas de ubicación: $locationCoordinates")
            
            // Create session with BusinessId and SiteId from BusinessContextManager
            android.util.Log.d("VehicleSessionRepo", "🆔 [startSession] Generando ID único para la sesión...")
            val sessionId = UUID.randomUUID().toString()
            android.util.Log.d("VehicleSessionRepo", "🆔 [startSession] Session ID generado: $sessionId")
            
            android.util.Log.d("VehicleSessionRepo", "📝 [startSession] Creando objeto VehicleSession...")
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
                siteId = siteId, // ✅ Use BusinessContextManager site ID
                initialHourMeter = initialHourMeter, // ✅ New: Store initial hour meter
                finalHourMeter = null,
                vehicle = vehicle
            )
            android.util.Log.d("VehicleSessionRepo", "📝 [startSession] VehicleSession creado exitosamente:")
            android.util.Log.d("VehicleSessionRepo", "  - ID: ${newSession.id}")
            android.util.Log.d("VehicleSessionRepo", "  - Vehicle ID: ${newSession.vehicleId}")
            android.util.Log.d("VehicleSessionRepo", "  - User ID: ${newSession.userId}")
            android.util.Log.d("VehicleSessionRepo", "  - Business ID: ${newSession.businessId}")
            android.util.Log.d("VehicleSessionRepo", "  - Site ID: ${newSession.siteId}")
            android.util.Log.d("VehicleSessionRepo", "  - Initial Hour Meter: ${newSession.initialHourMeter}")
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] === MAPEANDO A DTO ===")
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Convirtiendo VehicleSession a VehicleSessionDto...")
            val dto = VehicleSessionMapper.toDto(newSession)
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] DTO creado exitosamente:")
            android.util.Log.d("VehicleSessionRepo", "  - DTO ID: ${dto.Id}")
            android.util.Log.d("VehicleSessionRepo", "  - DTO Vehicle ID: ${dto.VehicleId}")
            android.util.Log.d("VehicleSessionRepo", "  - DTO Business ID: ${dto.BusinessId}")
            android.util.Log.d("VehicleSessionRepo", "  - DTO Site ID: ${dto.siteId}")
            android.util.Log.d("VehicleSessionRepo", "  - DTO Vehicle object: ${dto.Vehicle != null}")
            
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Serializando DTO a JSON...")
            val gson = Gson()
            val entityJson = gson.toJson(dto)
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] JSON generado exitosamente, longitud: ${entityJson.length}")
            
            // 🔍 LOG CRÍTICO: Verificar exactamente qué se está enviando al backend
            android.util.Log.d("VehicleSessionRepo", """
                🔍 PAYLOAD ENVIADO AL BACKEND PARA CREAR SESIÓN:
                - Session ID: ${dto.Id}
                - Vehicle ID: ${dto.VehicleId}
                - User ID: ${dto.GOUserId}
                - Check ID: ${dto.ChecklistAnswerId}
                - Business ID: ${dto.BusinessId}
                - Site ID: ${dto.siteId}
                - Start Time: ${dto.StartTime}
                - Status: ${dto.Status}
                - Vehicle object present: ${dto.Vehicle != null}
                - Vehicle photoModel: ${dto.Vehicle?.photoModel ?: "NULL"}
                - Vehicle codename: ${dto.Vehicle?.codename ?: "NULL"}
                - IsNew: ${dto.IsNew}
                - IsDirty: ${dto.IsDirty}
                - JSON length: ${entityJson.length}
                - JSON preview: ${entityJson.take(500)}...
            """.trimIndent())
            
            // 🔍 LOG CRÍTICO: Verificar que la imagen esté en el JSON
            if (entityJson.contains("photoModel") || entityJson.contains("pictureFileSize") || entityJson.contains("pictureInternalName")) {
                android.util.Log.d("VehicleSessionRepo", "✅ JSON contiene campos de imagen del vehículo")
            } else {
                android.util.Log.w("VehicleSessionRepo", "⚠️ JSON NO contiene campos de imagen del vehículo")
            }
            
            android.util.Log.d("VehicleSessionRepo", "🌐 [startSession] === PREPARANDO LLAMADA A LA API ===")
            android.util.Log.d("VehicleSessionRepo", "🌐 [startSession] Obteniendo tokens de autenticación...")
            val csrfToken = authDataStore.getCsrfToken()
            android.util.Log.d("VehicleSessionRepo", "🌐 [startSession] CSRF Token obtenido: ${csrfToken != null}")
            if (csrfToken == null) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] No hay CSRF token disponible")
                throw Exception("No CSRF token available")
            }
            
            val cookie = authDataStore.getAntiforgeryCookie()
            android.util.Log.d("VehicleSessionRepo", "🌐 [startSession] Cookie obtenida: ${cookie != null}")
            if (cookie == null) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] No hay cookie antiforgery disponible")
                throw Exception("No antiforgery cookie available")
            }
            
            android.util.Log.d("VehicleSessionRepo", "🌐 [startSession] Tokens obtenidos exitosamente:")
            android.util.Log.d("VehicleSessionRepo", "  - CSRF Token: ${csrfToken.take(20)}...")
            android.util.Log.d("VehicleSessionRepo", "  - Cookie: ${cookie.take(20)}...")
            android.util.Log.d("VehicleSessionRepo", "  - Business ID: '$businessId'")
            
            android.util.Log.d("VehicleSessionRepo", "🌐 [startSession] Llamando a la API para guardar la sesión...")
            val response = api.saveSession(
                csrfToken, 
                cookie, 
                entityJson, 
                businessId = businessId // Use BusinessContextManager business ID
            )
            
            // 🔍 LOG CRÍTICO: Verificar respuesta del backend
            android.util.Log.d("VehicleSessionRepo", """
                🔍 RESPUESTA DEL BACKEND:
                - Status Code: ${response.code()}
                - Is Successful: ${response.isSuccessful}
                - Response Body: ${response.body()}
                - Error Body: ${response.errorBody()?.string()}
            """.trimIndent())
            
            android.util.Log.d("VehicleSessionRepo", "📡 [startSession] === PROCESANDO RESPUESTA DE LA API ===")
            android.util.Log.d("VehicleSessionRepo", "📡 [startSession] Respuesta recibida de la API:")
            android.util.Log.d("VehicleSessionRepo", "  - Status Code: ${response.code()}")
            android.util.Log.d("VehicleSessionRepo", "  - Is Successful: ${response.isSuccessful}")
            android.util.Log.d("VehicleSessionRepo", "  - Response Body: ${response.body()}")
            android.util.Log.d("VehicleSessionRepo", "  - Error Body: ${response.errorBody()?.string()}")
            
            if (!response.isSuccessful) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] La API falló al crear la sesión")
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Haciendo rollback del estado del vehículo...")
                
                // Rollback vehicle status if session creation fails
                val rollbackResult = vehicleStatusRepository.updateVehicleStatus(
                    vehicleId = vehicleId,
                    status = VehicleStatus.AVAILABLE,
                    businessId = businessId ?: "",
                    siteId = siteId
                )
                android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Resultado del rollback: $rollbackResult")
                
                throw Exception("Failed to create session: ${response.code()}")
            }
            
            android.util.Log.d("VehicleSessionRepo", "✅ [startSession] Sesión creada exitosamente en el backend")
            
            // ✅ NEW: Update vehicle's CurrentHourMeter if initialHourMeter is provided
            android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] === ACTUALIZANDO HOUR METER DEL VEHÍCULO ===")
            if (!initialHourMeter.isNullOrBlank()) {
                android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Hour meter inicial proporcionado: '$initialHourMeter'")
                android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Actualizando CurrentHourMeter del vehículo...")
                
                try {
                    android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] Llamando a vehicleRepository.updateCurrentHourMeter...")
                    vehicleRepository.updateCurrentHourMeter(
                        vehicleId = vehicleId,
                        currentHourMeter = initialHourMeter,
                        businessId = businessId ?: ""
                    )
                    android.util.Log.d("VehicleSessionRepo", "✅ [startSession] CurrentHourMeter del vehículo actualizado exitosamente a: $initialHourMeter")
                } catch (e: Exception) {
                    android.util.Log.w("VehicleSessionRepo", "⚠️ [startSession] Falló la actualización del CurrentHourMeter: ${e.message}", e)
                    android.util.Log.w("VehicleSessionRepo", "⚠️ [startSession] Nota: No se lanza excepción para evitar fallar el proceso de creación de sesión")
                    android.util.Log.w("VehicleSessionRepo", "⚠️ [startSession] La sesión ya se creó exitosamente, esta es solo una actualización adicional")
                }
            } else {
                android.util.Log.d("VehicleSessionRepo", "⏰ [startSession] No se proporcionó hour meter inicial - saltando actualización del vehículo")
            }
            
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] === PROCESANDO RESPUESTA FINAL ===")
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Convirtiendo respuesta del backend a VehicleSession...")
            val createdSession = response.body()?.let { VehicleSessionMapper.toDomain(it) }
            android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Sesión creada desde el backend: ${createdSession != null}")
            if (createdSession != null) {
                android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Detalles de la sesión creada:")
                android.util.Log.d("VehicleSessionRepo", "  - ID: ${createdSession.id}")
                android.util.Log.d("VehicleSessionRepo", "  - Start Time: ${createdSession.startTime}")
                android.util.Log.d("VehicleSessionRepo", "  - Vehicle ID: ${createdSession.vehicleId}")
                android.util.Log.d("VehicleSessionRepo", "  - Business ID: ${createdSession.businessId}")
                android.util.Log.d("VehicleSessionRepo", "  - Site ID: ${createdSession.siteId}")
            }
            
            android.util.Log.d("VehicleSessionRepo", "✅ [startSession] === SESIÓN CREADA EXITOSAMENTE ===")
            return createdSession ?: throw Exception("Failed to start session: Empty response")
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] === ERROR DURANTE LA CREACIÓN DE SESIÓN ===")
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Excepción capturada: ${e.message}")
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Haciendo rollback del estado del vehículo...")
            
            // Revert vehicle status on failure
            try {
                val rollbackResult = vehicleStatusRepository.updateVehicleStatus(
                    vehicleId = vehicleId,
                    status = VehicleStatus.AVAILABLE,
                    businessId = businessId ?: "",
                    siteId = siteId
                )
                android.util.Log.d("VehicleSessionRepo", "🔄 [startSession] Resultado del rollback en catch: $rollbackResult")
            } catch (rollbackError: Exception) {
                android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Error durante el rollback: ${rollbackError.message}")
            }
            
            android.util.Log.e("VehicleSessionRepo", "❌ [startSession] Re-lanzando excepción original: ${e.message}")
            throw e
        }
    }

    override suspend fun endSession(
        sessionId: String, 
        closeMethod: VehicleSessionClosedMethod,
        adminId: String?,
        notes: String?,
        finalHourMeter: String?
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
        
        // ✅ VALIDATION: Validate final hour meter if provided
        if (!finalHourMeter.isNullOrBlank()) {
            android.util.Log.d("VehicleSessionRepo", "[endSession] finalHourMeter: '$finalHourMeter', initialHourMeter: '${existingSession.initialHourMeter}'")
            val validationResult = app.forku.core.validation.HourMeterValidator.validateFinalHourMeter(
                finalValue = finalHourMeter,
                initialValue = existingSession.initialHourMeter
            )
            
            if (!validationResult.isValid) {
                android.util.Log.e("VehicleSessionRepo", "❌ Final hour meter validation failed: ${validationResult.errorMessage}")
                throw Exception("Invalid final hour meter: ${validationResult.errorMessage}")
            }
            
            android.util.Log.d("VehicleSessionRepo", "✅ Final hour meter validation passed: ${validationResult.validatedValue}")
        }
            
        try {
            // Update vehicle status back to AVAILABLE
            android.util.Log.d("VehicleSessionRepo", """
                🔍 ANTES DE ACTUALIZAR ESTADO DEL VEHÍCULO:
                - Vehicle ID: ${existingSession.vehicleId}
                - Business ID: $businessId
                - Site ID: $siteId
                - Session ID: ${existingSession.id}
                - Vehicle object in session: ${existingSession.vehicle != null}
                - Vehicle photoModel: ${existingSession.vehicle?.photoModel ?: "NULL"}
            """.trimIndent())
            
            vehicleStatusRepository.updateVehicleStatus(
                vehicleId = existingSession.vehicleId,
                status = VehicleStatus.AVAILABLE,
                businessId = businessId ?: "",
                siteId = siteId
            )
            
            android.util.Log.d("VehicleSessionRepo", "✅ Estado del vehículo actualizado a AVAILABLE")
            
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
                durationMinutes = duration,
                finalHourMeter = finalHourMeter // ✅ New: Store final hour meter
            )
            
            // 🔍 LOG CRÍTICO: Verificar estado de la imagen del vehículo en la sesión actualizada
            android.util.Log.d("VehicleSessionRepo", """
                🔍 IMAGEN DEL VEHÍCULO EN SESIÓN ACTUALIZADA:
                - Vehicle ID: ${updatedSession.vehicleId}
                - Vehicle object present: ${updatedSession.vehicle != null}
                - Vehicle photoModel: ${updatedSession.vehicle?.photoModel ?: "NULL"}
            """.trimIndent())
            
            // Antes de enviar el DTO para cerrar sesión, asegúrate de que IsNew=false
            val dto = VehicleSessionMapper.toDto(updatedSession).copy(IsNew = false)
            
            // 🔍 LOG CRÍTICO: Verificar estado de la imagen del vehículo en el DTO final
            android.util.Log.d("VehicleSessionRepo", """
                🔍 IMAGEN DEL VEHÍCULO EN DTO FINAL:
                - Session ID: ${dto.Id}
                - Vehicle object in DTO: ${dto.Vehicle != null}
                - Vehicle photoModel: ${dto.Vehicle?.photoModel ?: "NULL"}
                - Vehicle codename: ${dto.Vehicle?.codename ?: "NULL"}
            """.trimIndent())
            
            val gson = Gson()
            val entityJson = gson.toJson(dto)
            android.util.Log.d("VehicleSessionRepo", "[endSession] Payload JSON enviado a la API: $entityJson")
            android.util.Log.d("VehicleSessionRepo", "[endSession] Campos clave: Id=${dto.Id}, Status=${dto.Status}, EndTime=${dto.EndTime}, VehicleSessionClosedMethod=${dto.VehicleSessionClosedMethod}, ClosedBy=${dto.ClosedBy}, IsNew=${dto.IsNew}")
            
            // 🔍 LOG CRÍTICO: Verificar que la imagen esté en el JSON
            if (entityJson.contains("photoModel") || entityJson.contains("pictureFileSize") || entityJson.contains("pictureInternalName")) {
                android.util.Log.d("VehicleSessionRepo", "✅ JSON contiene campos de imagen del vehículo")
            } else {
                android.util.Log.w("VehicleSessionRepo", "⚠️ JSON NO contiene campos de imagen del vehículo")
            }
            
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
            
            // ✅ NEW: Update vehicle's CurrentHourMeter if finalHourMeter is provided
            if (!finalHourMeter.isNullOrBlank()) {
                try {
                    android.util.Log.d("VehicleSessionRepo", "[endSession] Updating vehicle CurrentHourMeter to: $finalHourMeter")
                    vehicleRepository.updateCurrentHourMeter(
                        vehicleId = existingSession.vehicleId,
                        currentHourMeter = finalHourMeter,
                        businessId = businessId ?: ""
                    )
                    android.util.Log.d("VehicleSessionRepo", "[endSession] Successfully updated vehicle CurrentHourMeter")
                } catch (e: Exception) {
                    android.util.Log.w("VehicleSessionRepo", "[endSession] Failed to update vehicle CurrentHourMeter: ${e.message}", e)
                    // Note: We don't throw here to avoid failing the session end process
                    // The session is already ended successfully, this is just a bonus update
                }
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
                businessId = businessId, include = "Vehicle,GOUser"
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
            val response = api.getAllSessions(businessId = businessId ?: "", include = "GOUser,Vehicle")
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
            val response = api.getAllSessions(businessId = businessId ?: "", include = "Vehicle,GOUser")
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
            
            val response = api.getAllSessions(businessId = businessId ?: "", include = "Vehicle,GOUser")
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
    override suspend fun getOperatingSessionsCount(businessId: String, siteId: String?): Int {
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] === INICIANDO CONTEO EN REPOSITORY ===")
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] BusinessId recibido: '$businessId'")
        android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] SiteId filter recibido: '$siteId' (null = All Sites)")
        
        try {
            val csrfToken = authDataStore.getCsrfToken() ?: throw Exception("No CSRF token available")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] CSRF Token obtenido: ${csrfToken.take(10)}...")
            
            val cookie = authDataStore.getAntiforgeryCookie() ?: throw Exception("No antiforgery cookie available")
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] Cookie obtenido: ${cookie.take(20)}...")
            
            // ✅ FIXED: Use provided siteId parameter instead of business context
            // If siteId is null, it means "All Sites" - count all sites in the business
            android.util.Log.d("VehicleSessionRepo", "[getOperatingSessionsCount] Using filter siteId: '$siteId'")
            
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
            val response = api.getSessionById(sessionId, include = "ChecklistAnswer,Vehicle")
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
    override suspend fun getActiveSessionsWithRelatedData(businessId: String, siteId: String?): AdminDashboardData {
        android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] === 🚀 OPTIMIZED API CALL FOR ADMIN DASHBOARD ===")
        android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] BusinessId: '$businessId'")
        android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] SiteId filter: '$siteId' (null = All Sites)")
        
        return try {
            // ✅ FIXED: Use provided siteId filter instead of user's context
            // If siteId is null, it means "All Sites" - show all sites in the business
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] Using filter siteId: '$siteId'")
            
            // Single API call with all related data included
            val include = "GOUser,GOUser.UserRoleItems,Vehicle,Vehicle.VehicleType,ChecklistAnswer"
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 🌐 API call with include: '$include'")
            
            // ✅ FIX: Include SiteId in API filter for better performance and accuracy
            val filter = if (siteId != null && siteId.isNotBlank()) {
                "Status == 0 && EndTime == null && BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "Status == 0 && EndTime == null && BusinessId == Guid.Parse(\"$businessId\")"
            }
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 🔍 API Filter: '$filter'")
            
            val response = api.getAllSessions(
                businessId = businessId,
                include = include,
                filter = filter
            )
            
            android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 📡 Response: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("VehicleSessionRepo", "getAllSessions response received: ${response}")
                val sessionDtos = response.body()!!
                android.util.Log.d("VehicleSessionRepo", "getAllSessions received sessionDtos: ${sessionDtos}")

                android.util.Log.d("VehicleSessionRepo", "[getActiveSessionsWithRelatedData] 📊 Total sessions received: ${sessionDtos.size}")
                
                // ✅ FIX: Filter by exact business and site match for consistency
                val filteredDtos = sessionDtos.filter { dto -> 
                    val businessMatch = dto.BusinessId == businessId
                    val siteMatch = if (siteId != null && siteId.isNotBlank()) {
                        dto.siteId == siteId
                    } else {
                        true // If no specific site is selected, show all for this business
                    }
                    val statusMatch = dto.Status == 0 && dto.EndTime == null
                    
                    android.util.Log.d("VehicleSessionRepo", "[FILTER-DEBUG] Session ${dto.Id}: businessMatch=$businessMatch (${dto.BusinessId} == $businessId), siteMatch=$siteMatch (${dto.siteId} == $siteId), statusMatch=$statusMatch")
                    
                    businessMatch && siteMatch && statusMatch
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