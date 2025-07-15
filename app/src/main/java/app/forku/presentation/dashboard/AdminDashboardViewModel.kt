package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import javax.inject.Inject
import app.forku.presentation.common.utils.parseDateTime
import kotlinx.coroutines.flow.update
import java.time.OffsetDateTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import app.forku.core.Constants
import app.forku.data.datastore.AuthDataStore
import app.forku.data.service.GOServicesManager
import app.forku.data.api.VehicleSessionApi
import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.usecase.incident.GetUserIncidentCountUseCase
import app.forku.domain.usecase.incident.GetAdminIncidentCountUseCase
import app.forku.domain.usecase.safetyalert.GetSafetyAlertCountUseCase
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase
import app.forku.domain.repository.user.UserPreferencesRepository
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel

sealed class AuthEvent {
    object NavigateToLogin : AuthEvent()
}

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val incidentRepository: IncidentRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val userRepository: UserRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val authDataStore: AuthDataStore,
    private val goServicesManager: GOServicesManager,
    private val vehicleSessionApi: VehicleSessionApi,
    private val getUserIncidentCountUseCase: GetUserIncidentCountUseCase,
    private val getAdminIncidentCountUseCase: GetAdminIncidentCountUseCase,
    private val getSafetyAlertCountUseCase: GetSafetyAlertCountUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _checklistAnswers = MutableStateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>>(emptyMap())
    val checklistAnswers: StateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>> = _checklistAnswers.asStateFlow()

    // ✅ REMOVED: businessContextState - Admin doesn't use business context

    init {
        loadCurrentUser()
        // ✅ Admin doesn't need business context - all data loading is filter-based
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error loading user: ${e.message}"
                )
            }
        }
    }

    private suspend fun getVehicleSessionInfo(session: VehicleSession): VehicleSessionInfo? {
        return try {
            android.util.Log.d("AdminDashboard", "Getting info for session: $session")
            val currentUser = userRepository.getCurrentUser()
            val businessId = currentUser?.businessId
            
            if (businessId == null) {
                android.util.Log.e("AdminDashboard", "No business context available")
                return null
            }
            
            val vehicle = vehicleRepository.getVehicle(session.vehicleId, businessId)
            android.util.Log.d("AdminDashboard", "Got vehicle: $vehicle")
            
            val operator = userRepository.getUserById(session.userId)
            android.util.Log.d("AdminDashboard", "Got operator: $operator, photoUrl: ${operator?.photoUrl}")

            // Calculate session progress (assuming 8-hour default duration)
            val startTime = parseDateTime(session.startTime)
            val now = OffsetDateTime.now()
            val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
            val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)

            // Remove defaultAvatarUrl and only assign operatorImage if photoUrl is not blank
            VehicleSessionInfo(
                vehicle = vehicle,
                vehicleId = vehicle.id,
                vehicleType = vehicle.type.Name,
                codename = vehicle.codename,
                vehicleImage = vehicle.photoModel,
                session = session,
                operator = operator,
                operatorName = "${operator?.firstName?.firstOrNull() ?: ""}. ${operator?.lastName ?: ""}",
                operatorImage = operator?.photoUrl?.takeIf { !it.isNullOrBlank() },
                sessionStartTime = session.startTime,
                userRole = operator?.role ?: UserRole.OPERATOR,
                progress = progress
            )
        } catch (e: Exception) {
            android.util.Log.e("AdminDashboard", "Error getting session info: ${e.message}")
            null
        }
    }

    private suspend fun getOperatorSessionInfo(session: VehicleSession): OperatorSessionInfo? {
        return try {
            val operator = userRepository.getUserById(session.userId)
            operator?.let {
                OperatorSessionInfo(
                    name = "${it.firstName.firstOrNull() ?: ""}. ${it.lastName}",
                    fullName = it.fullName,
                    username = it.username,
                    image = it.photoUrl?.takeIf { url -> !url.isNullOrBlank() },
                    isActive = true, // This operator has an active session since we're getting info from a session
                    userId = it.id,
                    sessionStartTime = session.startTime,
                    role = it.role
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load incident count specifically for Admin Dashboard
     * This method is ONLY for Admin users and counts ALL incidents in the business context
     * - For "All Sites": counts ALL incidents from ALL sites in the business
     * - For specific site: counts ALL incidents from that specific site
     */
    private fun loadIncidentCountForAdminDashboard(user: User?, businessId: String?, siteId: String?) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] === 🚀 INICIANDO CARGA DE CONTADOR DE INCIDENTES (ADMIN DASHBOARD) ===")
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 📍 CONTEXT: This method is ONLY for Admin Dashboard")
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] INPUTS: user=${user?.id}, role=${user?.role}, businessId=$businessId, siteId=$siteId")
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] USER DETAILS: name=${user?.fullName}, username=${user?.username}")
                
                // Only proceed if user is Admin
                if (user?.role != UserRole.ADMIN) {
                    android.util.Log.w("AdminDashboard", "[loadIncidentCountForAdminDashboard] ⚠️ User is not Admin (${user?.role}), skipping incident count")
                    _state.update { it.copy(
                        userIncidentsCount = 0,
                        totalIncidentsCount = 0
                    ) }
                    return@launch
                }
                
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 🎯 ADMIN CONFIRMED: Proceeding with Admin incident count logic")
                
                // For Admin Dashboard: always count ALL incidents (not user-specific)
                val count = if (siteId == null) {
                    // "All Sites" selected - count ALL incidents from ALL sites in business
                    android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 🌐 ALL SITES MODE: Counting ALL incidents from ALL sites in business")
                    android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 📊 ALL SITES LOGIC: Will count incidents from all users, all sites in business")
                    getAdminIncidentCountUseCase(businessId, null).also {
                        android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] ✅ ALL SITES RESULT: getAdminIncidentCountUseCase($businessId, null) = $it")
                    }
                } else {
                    // Specific site selected - count ALL incidents from that specific site
                    android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 🎯 SPECIFIC SITE MODE: Counting ALL incidents from site: $siteId")
                    android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 📊 SPECIFIC SITE LOGIC: Will count incidents from all users in specific site")
                    getAdminIncidentCountUseCase(businessId, siteId).also {
                        android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] ✅ SPECIFIC SITE RESULT: getAdminIncidentCountUseCase($businessId, $siteId) = $it")
                    }
                }
                
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] 🎯 FINAL COUNT: $count incidents")
                android.util.Log.d("AdminDashboard", "[loadIncidentCountForAdminDashboard] === ✅ CONTADOR DE INCIDENTES CARGADO (ADMIN DASHBOARD) ===")
                
                _state.update { it.copy(
                    userIncidentsCount = count,
                    totalIncidentsCount = count
                ) }
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "[loadIncidentCountForAdminDashboard] ❌ EXCEPTION: ${e.message}", e)
                _state.update { it.copy(
                    userIncidentsCount = 0,
                    totalIncidentsCount = 0
                ) }
            }
        }
    }

    // ✅ REMOVED: loadDashboardData() - Admin always uses loadDashboardDataWithFilters()
    // This prevents confusion and ensures Admin never uses user context

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // ✅ NEW: Load dashboard data with specific filters
    fun loadDashboardDataWithFilters(filterBusinessId: String?, filterSiteId: String?) {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] === 🚀 INICIANDO CARGA CON FILTROS ESPECÍFICOS ===")
            android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] INPUTS: filterBusinessId=$filterBusinessId, filterSiteId=$filterSiteId")
            try {
                _state.value = _state.value.copy(isLoading = true)

                val currentUser = userRepository.getCurrentUser()
                
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 🏢 Filter BusinessId: '$filterBusinessId'")
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 🏭 Filter SiteId: '$filterSiteId' (null = All Sites)")
                
                if (currentUser != null) {
                    android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 👤 Calling loadIncidentCountForAdminDashboard for user: ${currentUser.id} (${currentUser.role})")
                    android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 📊 Incident count parameters: businessId=$filterBusinessId, siteId=$filterSiteId")
                    loadIncidentCountForAdminDashboard(currentUser, filterBusinessId, filterSiteId)
                } else {
                    android.util.Log.w("AdminDashboard", "[loadDashboardDataWithFilters] ⚠️ No current user found, skipping incident count")
                }

                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 🌐 Llamando a getActiveSessionsWithRelatedData con businessId: '$filterBusinessId', siteId: '$filterSiteId'")
                
                // 🚀 OPTIMIZED: Single API call with all related data
                // ✅ FIXED: Pass the filterSiteId to respect "All Sites" selection
                val activeSessions = vehicleSessionRepository.getActiveSessionsWithRelatedData(
                    businessId = filterBusinessId ?: "",
                    siteId = filterSiteId // ✅ This will be null for "All Sites"
                )
                
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] ✅ Active sessions loaded: ${activeSessions.activeSessions.size}")
                
                // Create active operators from optimized data (no additional API calls!)
                val activeOperators = activeSessions.activeSessions.mapNotNull { session ->
                    try {
                        val operator = activeSessions.operators[session.userId] ?: app.forku.domain.model.user.User(
                            id = session.userId,
                            token = "",
                            refreshToken = "",
                            email = "",
                            username = session.operatorName ?: "Sin nombre",
                            firstName = session.operatorName ?: "",
                            lastName = "",
                            photoUrl = null,
                            role = UserRole.OPERATOR,
                            certifications = emptyList(),
                            points = 0,
                            totalHours = 0f,
                            totalDistance = 0,
                            sessionsCompleted = 0,
                            incidentsReported = 0,
                            lastMedicalCheck = null,
                            lastLogin = null,
                            isActive = true,
                            isApproved = false,
                            password = "",
                            businessId = null,
                            siteId = null,
                            systemOwnerId = null,
                            userPreferencesId = null
                        )
                        val displayName = listOfNotNull(operator.firstName, operator.lastName)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { operator.username ?: "Sin nombre" }
                        android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] ✅ Operator from optimized data: $displayName")
                        OperatorSessionInfo(
                            name = displayName,
                            fullName = operator.fullName,
                            username = operator.username,
                            image = operator.photoUrl?.takeIf { !it.isNullOrBlank() },
                            isActive = true,
                            userId = operator.id,
                            sessionStartTime = session.startTime,
                            role = operator.role
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AdminDashboard", "[loadDashboardDataWithFilters] ❌ Error creating operator info: ${e.message}", e)
                        null
                    }
                }

                // Get other dashboard data (safety alerts)
                val safetyAlertsCount = getSafetyAlertCountUseCase(filterBusinessId, filterSiteId)

                // ✅ UPDATED: Use repository method to get operating vehicles count with filters for consistency
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 🚗 Getting operating vehicles count with filters: businessId='$filterBusinessId', siteId='$filterSiteId'")
                val operatingVehiclesCount = vehicleSessionRepository.getOperatingSessionsCount(filterBusinessId ?: "", filterSiteId)
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 🚗 Operating vehicles count from repository: $operatingVehiclesCount")

                _state.value = _state.value.copy(
                    operatingVehiclesCount = operatingVehiclesCount,
                    safetyAlertsCount = safetyAlertsCount,
                    activeVehicleSessions = activeSessions.activeSessions.map { session ->
                        // Puedes usar aquí el mismo mapeo que en loadDashboardData para crear VehicleSessionInfo
                        // Ejemplo básico:
                        VehicleSessionInfo(
                            vehicle = activeSessions.vehicles[session.vehicleId]!!,
                            vehicleId = session.vehicleId,
                            vehicleType = activeSessions.vehicles[session.vehicleId]?.type?.Name,
                            codename = activeSessions.vehicles[session.vehicleId]?.codename,
                            vehicleImage = activeSessions.vehicles[session.vehicleId]?.photoModel,
                            session = session,
                            operator = activeSessions.operators[session.userId],
                            operatorName = activeSessions.operators[session.userId]?.let { op ->
                                listOfNotNull(op.firstName, op.lastName).joinToString(" ").ifBlank { op.username ?: "Sin nombre" }
                            } ?: session.operatorName,
                            operatorImage = activeSessions.operators[session.userId]?.photoUrl?.takeIf { !it.isNullOrBlank() },
                            sessionStartTime = session.startTime,
                            userRole = activeSessions.operators[session.userId]?.role ?: UserRole.OPERATOR,
                            progress = null // Puedes calcular el progreso si lo necesitas
                        )
                    },
                    activeOperators = activeOperators,
                    isLoading = false,
                    error = null
                )
                
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] === 🎉 DASHBOARD CON FILTROS CARGADO ===")
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] ✅ Sessions: ${activeSessions.activeSessions.size}, Operators: ${activeOperators.size}")
                android.util.Log.d("AdminDashboard", "[loadDashboardDataWithFilters] 🚗 Operating Vehicles: $operatingVehiclesCount")
                
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "[loadDashboardDataWithFilters] ❌ Error: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Failed to load dashboard data with filters. Please try again.",
                    isLoading = false
                )
            }
        }
    }

    // ✅ CENTRALIZED: Single refresh function for Admin Dashboard
    // Admin always uses filters, never context
    fun refreshDashboard(
        filterBusinessId: String?, 
        filterSiteId: String?, 
        showLoading: Boolean = true
    ) {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[refreshDashboard] === 🔄 CENTRALIZED REFRESH ===")
            android.util.Log.d("AdminDashboard", "[refreshDashboard] Filters: businessId=$filterBusinessId, siteId=$filterSiteId")
            android.util.Log.d("AdminDashboard", "[refreshDashboard] showLoading=$showLoading")
            
            try {
                if (showLoading) {
                    _state.value = _state.value.copy(isLoading = true)
                }
                
                // Load dashboard data with filters ONLY
                loadDashboardDataWithFilters(filterBusinessId, filterSiteId)
                
                android.util.Log.d("AdminDashboard", "[refreshDashboard] === ✅ REFRESH COMPLETED ===")
                
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "[refreshDashboard] ❌ Error: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Failed to refresh dashboard: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    // ✅ CONVENIENCE: Legacy functions that call the centralized refresh
    fun refreshWithLoading(filterBusinessId: String?, filterSiteId: String?) {
        refreshDashboard(filterBusinessId, filterSiteId, showLoading = true)
    }
    
    fun refresh(filterBusinessId: String?, filterSiteId: String?) {
        refreshDashboard(filterBusinessId, filterSiteId, showLoading = false)
    }

    fun submitFeedback(rating: Int, feedback: String, canContactMe: Boolean) {
        viewModelScope.launch {
            try {
                submitFeedbackUseCase(rating, feedback, canContactMe)
                _state.update { it.copy(feedbackSubmitted = true) }
                delay(3000)
                _state.update { it.copy(feedbackSubmitted = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to submit feedback: ${e.message}") }
            }
        }
    }

    /**
     * ✅ REMOVED: refreshBusinessContext() - Admin doesn't need business context refresh
     * Admin always uses filters, never context
     */
    // This function was removed because Admin should never use business context
    // All data loading should be based on explicit filters

    // ✅ UPDATED: Method to get operating vehicles count using admin filters
    fun loadOperatingVehiclesCount(
        filterBusinessId: String? = null,
        filterSiteId: String? = null,
        isAllSitesSelected: Boolean = false
    ) {
        viewModelScope.launch {
            android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] === INICIANDO CONTEO DE VEHÍCULOS OPERANDO ===")
            try {
                // Get current user to check if admin
                val currentUser = userRepository.getCurrentUser()
                val isAdmin = currentUser?.role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
                
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] User role: ${currentUser?.role}, isAdmin: $isAdmin")
                
                // Get business ID and site filter based on user role
                val businessId: String
                val siteId: String?

                // For Admin: use filter business ID and site filter
                businessId = filterBusinessId ?: ""
                siteId = if (isAllSitesSelected) null else filterSiteId

                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] 🎯 ADMIN MODE:")
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   - Filter BusinessId: '$filterBusinessId'")
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   - Filter SiteId: '$filterSiteId'")
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   - Is All Sites Selected: $isAllSitesSelected")
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   - Effective BusinessId: '$businessId'")
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   - Effective SiteId: '$siteId' (null = All Sites)")


                if (businessId.isBlank()) {
                    android.util.Log.w("AdminDashboard", "[loadOperatingVehiclesCount] BusinessId is empty, cannot get count")
                    _state.value = _state.value.copy(
                        operatingVehiclesCount = 0,
                        error = "No business context available"
                    )
                    return@launch
                }
                
                // Log current active sessions for comparison
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Sesiones activas actuales en UI: ${_state.value.activeVehicleSessions.size}")
                _state.value.activeVehicleSessions.forEachIndexed { index, session ->
                    android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount]   Sesión $index: vehicleId=${session.vehicleId}, codename=${session.codename}, sessionId=${session.session.id}")
                }
                
                // ✅ UPDATED: Use repository method with business context AND site filter
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] Llamando a vehicleSessionRepository.getOperatingSessionsCount('$businessId', '$siteId')...")
                val count = vehicleSessionRepository.getOperatingSessionsCount(businessId, siteId)
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] ✅ Conteo recibido para business '$businessId', site '$siteId': $count")
                
                _state.value = _state.value.copy(
                    operatingVehiclesCount = count,
                    isLoading = false,
                    error = null
                )
                
                android.util.Log.d("AdminDashboard", "[loadOperatingVehiclesCount] === CONTEO COMPLETADO: $count vehículos operando ===")
                
            } catch (e: Exception) {
                android.util.Log.e("AdminDashboard", "[loadOperatingVehiclesCount] ❌ Error: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Error loading operating vehicles count: ${e.message}",
                    operatingVehiclesCount = 0,
                    isLoading = false
                )
            }
        }
    }

    /**
     * ✅ REMOVED: reloadData() - Admin should never use business context
     * Admin always uses filters, never context
     */
    fun reloadData() {
        android.util.Log.e("AdminDashboard", "[reloadData] ❌ ERROR: Admin should never use reloadData()")
        android.util.Log.e("AdminDashboard", "[reloadData] Admin must use refreshDashboard() with explicit filter parameters")
        android.util.Log.e("AdminDashboard", "[reloadData] This method uses business context which is incorrect for Admin")
        
        // ❌ ERROR: Admin should never use business context
        // This method is intentionally left empty to prevent incorrect usage
        _state.value = _state.value.copy(
            error = "Admin must use filters, not business context. Use refreshDashboard() instead."
        )
    }
} 