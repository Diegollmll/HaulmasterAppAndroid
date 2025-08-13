package app.forku.presentation.vehicle.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.core.Constants
import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject
import app.forku.presentation.common.utils.parseDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import app.forku.core.auth.HeaderManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import app.forku.core.business.BusinessContextManager
import app.forku.data.mapper.toDomain
import app.forku.domain.repository.site.SiteRepository
import app.forku.domain.model.Site
import app.forku.presentation.common.components.BusinessContextUpdater
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext
import app.forku.domain.repository.user.UserPreferencesRepository

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val headerManager: HeaderManager,
    override val businessContextManager: BusinessContextManager,
    private val siteRepository: SiteRepository,
    override val userPreferencesRepository: UserPreferencesRepository
) : ViewModel(), BusinessContextUpdater {
    private val _state = MutableStateFlow(VehicleListState())
    val state = _state.asStateFlow()
    
    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    private val _checklistAnswers = MutableStateFlow<Map<String, app.forku.domain.model.checklist.ChecklistAnswer>>(emptyMap())
    val checklistAnswers = _checklistAnswers.asStateFlow()

    // Business context from BusinessContextManager
    val businessContextState = businessContextManager.contextState

    // Add loading flag to prevent concurrent loads
    private var isLoading = false
    
    // ✅ NEW: Admin mode flag to block automatic context-based loading
    private var isAdminMode = false

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        // Loguear si se desea, pero dejar que TokenErrorHandler y BaseScreen manejen la navegación
    }

    sealed class AuthEvent {
        object NavigateToLogin : AuthEvent()
    }

    init {
        loadCurrentUser()
        observeAuthState()
        observeBusinessContextChanges()
        viewModelScope.launch {
            businessContextManager.loadBusinessContext()
            loadSites()
        }
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                // Handle error silently as this is not critical
            }
        }
    }
    


    private fun observeAuthState() {
        viewModelScope.launch {
            headerManager.authState.collect { authState ->
                when (authState) {
                    is HeaderManager.AuthState.NotAuthenticated,
                    is HeaderManager.AuthState.TokenExpired -> {
                        _authEvent.emit(AuthEvent.NavigateToLogin)
                    }
                    else -> { /* No action needed */ }
                }
            }
        }
    }

    private fun loadSites() {
        Log.d("VehicleListViewModel", "VehicleListViewModel loadSites A")
        viewModelScope.launch {
            try {
                // ✅ FIXED: Use correct method based on admin mode
                val businessId = businessContextManager.getCurrentBusinessId()
                Log.d("VehicleListViewModel", "VehicleListViewModel loadSites B businessId: $businessId")
                if (isAdminMode && businessId != null) {
                    // Admin mode: load ALL sites for the business (for filtering)
                    Log.d("VehicleListViewModel", "🔧 ADMIN MODE: Loading all sites for business: $businessId")
                    siteRepository.getAllSites().collect { result ->
                        result.fold(
                            onSuccess = { sitesDto ->
                                val sites = sitesDto.map { it.toDomain() }
                                _state.value = _state.value.copy(availableSites = sites)
                                Log.d("VehicleListViewModel", "✅ Admin mode: Loaded ${sites.size} sites for filtering")
                            },
                            onFailure = { error ->
                                Log.e("VehicleListViewModel", "❌ Admin mode: Error loading sites for business", error)
                            }
                        )
                    }
                } else {
                    // Operator mode: load only user's assigned sites (context-based)
                    Log.d("VehicleListViewModel", "👤 OPERATOR MODE: Loading user assigned sites")
                    siteRepository.getUserAssignedSites(businessId ?: "").collect { result ->
                        result.fold(
                            onSuccess = { sitesDto ->
                                val sites = sitesDto.map { it.toDomain() }
                                _state.value = _state.value.copy(availableSites = sites)
                                Log.d("VehicleListViewModel", "✅ Operator mode: Loaded ${sites.size} assigned sites")
                            },
                            onFailure = { error ->
                                Log.e("VehicleListViewModel", "❌ Operator mode: Error loading assigned sites", error)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VehicleListViewModel", "❌ Error in loadSites", e)
            }
        }
    }

    fun selectSite(siteId: String?) {
        businessContextManager.setCurrentSiteId(siteId)
        _state.value = _state.value.copy(selectedSiteId = siteId)
        loadVehicles(showLoading = true)
    }

    fun loadVehicles(showLoading: Boolean = true) {
        Log.d("VehicleListViewModel", "[LOG] INICIO loadVehicles() -> showLoading=$showLoading, isAdminMode=$isAdminMode, isLoading=$isLoading")
        if (isLoading) {
            Log.d("VehicleListViewModel", "[LOG] loadVehicles: isLoading=true, return early")
            return
        }
        
        viewModelScope.launch(exceptionHandler) {
            try {
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: Lanzando coroutine")
                isLoading = true
                _state.value = _state.value.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                )
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: Estado actualizado: isLoading=$showLoading, isRefreshing=$showLoading")

                val businessId = businessContextManager.getCurrentBusinessId()
                val siteId = businessContextManager.getCurrentSiteId()
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: businessId=$businessId, siteId=$siteId (context-based)")
                Log.d("VehicleListViewModel", "[MULTITENANCY] Loading vehicles for businessId: $businessId, siteId: $siteId")
                
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: Llamando vehicleRepository.getVehiclesWithRelatedData(businessId=$businessId, siteId=$siteId)")
                val vehiclesWithData = try {
                    vehicleRepository.getVehiclesWithRelatedData(businessId ?: "", siteId)
                } catch (e: retrofit2.HttpException) {
                    Log.e("VehicleListViewModel", "[LOG] loadVehicles: HttpException: ${e.code()} - ${e.message()}")
                    if (e.code() == 401 || e.code() == 403) {
                        Log.e("VehicleListViewModel", "[LOG] loadVehicles: HttpException 401/403, rethrow")
                        throw e
                    }
                    _state.value = _state.value.copy(
                        error = "Error loading vehicles: ${e.message()}",
                        isLoading = false,
                        isRefreshing = false
                    )
                    isLoading = false
                    Log.d("VehicleListViewModel", "[LOG] loadVehicles: Estado actualizado por error HttpException")
                    return@launch
                } catch (e: Exception) {
                    Log.e("VehicleListViewModel", "[LOG] loadVehicles: Exception: ${e.message}")
                    when {
                        e.message?.contains("Unauthorized") == true || 
                        e.message?.contains("Token expired") == true ||
                        e.message?.contains("Session expired") == true -> {
                            Log.w("VehicleListViewModel", "[LOG] loadVehicles: Session expired - redirecting to login")
                            _authEvent.emit(AuthEvent.NavigateToLogin)
                            return@launch
                        }
                        e.message?.contains("No application token found") == true -> {
                            Log.w("VehicleListViewModel", "[LOG] loadVehicles: No application token found - letting BaseScreen handle auth")
                            _state.value = _state.value.copy(
                                error = "Authentication required. Please wait...",
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                        else -> {
                            Log.e("VehicleListViewModel", "[LOG] loadVehicles: Otro error: ${e.message}")
                            _state.value = _state.value.copy(
                                error = "Error loading vehicles: ${e.message}",
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                    isLoading = false
                    Log.d("VehicleListViewModel", "[LOG] loadVehicles: Estado actualizado por error Exception")
                    return@launch
                }

                Log.d("VehicleListViewModel", "[LOG] loadVehicles: vehiclesWithData.size = ${vehiclesWithData.size}")
                // Transform the optimized data to the existing state structure
                val vehicles = vehiclesWithData.map { it.vehicle }
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: vehicles.size = ${vehicles.size}")
                val activeSessions = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.activeSessions.firstOrNull { 
                        it.sessionStartTime != null 
                    }
                }.filterValues { it != null }.mapValues { it.value!! }
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: activeSessions.size = ${activeSessions.size}")
                
                val lastChecks = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.lastPreShiftCheck?.let { checklistAnswer ->
                        // Convert ChecklistAnswer to PreShiftCheck for compatibility
                        app.forku.domain.model.checklist.PreShiftCheck(
                            id = checklistAnswer.id,
                            vehicleId = checklistAnswer.vehicleId ?: vehicleData.vehicle.id,
                            userId = checklistAnswer.goUserId,
                            status = checklistAnswer.status.toString(),
                            startDateTime = checklistAnswer.startDateTime,
                            endDateTime = checklistAnswer.endDateTime,
                            items = emptyList() // Will be populated if needed
                        )
                    }
                }
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: lastChecks.size = ${lastChecks.size}")
                
                val checklistAnswers = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.checklistAnswers
                        .sortedByDescending { 
                            // Use same logic as repository: lastCheckDateTime or endDateTime as fallback
                            it.lastCheckDateTime.takeIf { it.isNotBlank() } ?: it.endDateTime 
                        }
                        .firstOrNull()
                }.filterValues { it != null }.mapValues { it.value!! }
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: checklistAnswers.size = ${checklistAnswers.size}")

                // Update state with all data (same structure as before, but from optimized source)
                _state.value = _state.value.copy(
                    vehicles = vehicles,
                    vehicleSessions = activeSessions,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                    currentBusinessId = businessId,
                    selectedSiteId = siteId, // ✅ Update selectedSiteId from BusinessContextManager
                    hasBusinessContext = businessContextManager.hasRealBusinessContext()
                )
                _checklistAnswers.value = checklistAnswers
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: Estado final actualizado: vehicles=${vehicles.size}, sessions=${activeSessions.size}, lastChecks=${lastChecks.size}")

                Log.d("VehicleListViewModel", "[MULTITENANCY] Loaded ${vehicles.size} vehicles for businessId: $businessId, siteId: $siteId")

            } catch (e: Exception) {
                Log.e("VehicleListViewModel", "[LOG] loadVehicles: Exception final: ${e.message}")
                _state.value = _state.value.copy(
                    error = "Error loading vehicles. Please try again later.",
                    isLoading = false,
                    isRefreshing = false
                )
            } finally {
                isLoading = false
                Log.d("VehicleListViewModel", "[LOG] loadVehicles: FINALLY: isLoading=false")
            }
        }
    }

    fun refresh() {
        loadVehicles(showLoading = false)
    }

    fun refreshWithLoading() {
        loadVehicles(showLoading = true)
    }
    
    /**
     * Refresh business context and reload vehicles
     * Useful when user switches business or business assignment changes
     */
    fun refreshBusinessContext() {
        viewModelScope.launch {
            try {
                Log.d("VehicleListViewModel", "Refreshing business context...")
                
                // Use BusinessContextManager to refresh context
                businessContextManager.refreshBusinessContext()
                
                // Reload vehicles with new context
                loadVehicles(showLoading = true)
                
            } catch (e: Exception) {
                Log.e("VehicleListViewModel", "Error refreshing business context: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Failed to refresh business context: ${e.message}"
                )
            }
        }
    }

    private fun observeBusinessContextChanges() {
        viewModelScope.launch {
            businessContextManager.contextState.collect { contextState ->
                Log.d("VehicleListViewModel", "Business context changed: businessId=${contextState.businessId}, siteId=${contextState.siteId}")
                
                // Update the local state with new context
                _state.value = _state.value.copy(
                    currentBusinessId = contextState.businessId,
                    selectedSiteId = contextState.siteId,
                    hasBusinessContext = contextState.hasRealBusinessContext
                )
                
                // ✅ CRITICAL FIX: Check if current user is admin BEFORE deciding to reload
                val currentUser = _currentUser.value
                val isUserAdmin = currentUser?.role in listOf(
                    app.forku.domain.model.user.UserRole.ADMIN,
                    app.forku.domain.model.user.UserRole.SUPERADMIN,
                    app.forku.domain.model.user.UserRole.SYSTEM_OWNER
                )
                
                Log.d("VehicleListViewModel", "🔍 Context change check: isUserAdmin=$isUserAdmin, isAdminMode=$isAdminMode, hasRealBusinessContext=${contextState.hasRealBusinessContext}, isLoading=${contextState.isLoading}")
                
                // ✅ CRITICAL FIX: Only reload vehicles if user is NOT admin and we have a valid business context
                // and this is not the initial load (to avoid double loading)
                if (!isUserAdmin && !isAdminMode && contextState.hasRealBusinessContext && !contextState.isLoading) {
                    Log.d("VehicleListViewModel", "Reloading vehicles due to context change (operator mode)")
                    loadVehicles(showLoading = false) // Silent reload to avoid UI flicker
                } else if (isUserAdmin || isAdminMode) {
                    Log.d("VehicleListViewModel", "🚫 ADMIN USER/MODE: Skipping automatic reload due to context change")
                } else {
                    Log.d("VehicleListViewModel", "🚫 Skipping reload: isUserAdmin=$isUserAdmin, isAdminMode=$isAdminMode, hasRealBusinessContext=${contextState.hasRealBusinessContext}, isLoading=${contextState.isLoading}")
                }
            }
        }
    }

    /**
     * Implementation of BusinessContextUpdater interface
     * Reloads vehicles when context changes
     */
    override fun reloadData() {
        loadVehicles(showLoading = true)
    }

    /**
     * ✅ NEW: Load vehicles with temporary filters (VIEW_FILTER mode)
     * Does NOT change user's personal context/preferences
     * Used for admin filtering across different sites
     */
    fun loadVehiclesWithFilters(filterBusinessId: String?, filterSiteId: String?, isAllSitesSelected: Boolean = false) {
        Log.d("VehicleListViewModel", "[LOG] INICIO loadVehiclesWithFilters() -> filterBusinessId=$filterBusinessId, filterSiteId=$filterSiteId, isAllSitesSelected=$isAllSitesSelected")
        if (isLoading) {
            Log.d("VehicleListViewModel", "[LOG] isLoading=true, return early")
            return
        }
        viewModelScope.launch(exceptionHandler) {
            try {
                Log.d("VehicleListViewModel", "[LOG] Lanzando coroutine para loadVehiclesWithFilters")
                isLoading = true
                _state.value = _state.value.copy(
                    isLoading = true,
                    isRefreshing = true
                )
                Log.d("VehicleListViewModel", "[LOG] Estado actualizado: isLoading=true, isRefreshing=true")

                val businessId = filterBusinessId ?: businessContextManager.getCurrentBusinessId()
                Log.d("VehicleListViewModel", "[LOG] businessId calculado: $businessId (filterBusinessId=$filterBusinessId, context=${businessContextManager.getCurrentBusinessId()})")
                val siteId = if (isAllSitesSelected) null else filterSiteId
                Log.d("VehicleListViewModel", "[LOG] siteId calculado: $siteId (isAllSitesSelected=$isAllSitesSelected, filterSiteId=$filterSiteId)")

                Log.d("VehicleListViewModel", "[LOG] Llamando vehicleRepository.getVehiclesWithRelatedData(businessId=$businessId, siteId=$siteId)")
                val vehiclesWithData = try {
                    vehicleRepository.getVehiclesWithRelatedData(businessId ?: "", siteId)
                } catch (e: retrofit2.HttpException) {
                    Log.e("VehicleListViewModel", "[LOG] HttpException: ${e.code()} - ${e.message()}")
                    if (e.code() == 401 || e.code() == 403) {
                        Log.e("VehicleListViewModel", "[LOG] HttpException 401/403, rethrow")
                        throw e
                    }
                    _state.value = _state.value.copy(
                        error = "Error loading vehicles: ${e.message()}",
                        isLoading = false,
                        isRefreshing = false
                    )
                    isLoading = false
                    Log.d("VehicleListViewModel", "[LOG] Estado actualizado por error HttpException")
                    return@launch
                } catch (e: Exception) {
                    Log.e("VehicleListViewModel", "[LOG] Exception: ${e.message}")
                    when {
                        e.message?.contains("Unauthorized") == true || 
                        e.message?.contains("Token expired") == true ||
                        e.message?.contains("Session expired") == true -> {
                            Log.w("VehicleListViewModel", "[LOG] Session expired - redirecting to login")
                            _authEvent.emit(AuthEvent.NavigateToLogin)
                            return@launch
                        }
                        e.message?.contains("No application token found") == true -> {
                            Log.w("VehicleListViewModel", "[LOG] No application token found - letting BaseScreen handle auth")
                            _state.value = _state.value.copy(
                                error = "Authentication required. Please wait...",
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                        else -> {
                            Log.e("VehicleListViewModel", "[LOG] Otro error: ${e.message}")
                            _state.value = _state.value.copy(
                                error = "Error loading vehicles: ${e.message}",
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                    isLoading = false
                    Log.d("VehicleListViewModel", "[LOG] Estado actualizado por error Exception")
                    return@launch
                }

                Log.d("VehicleListViewModel", "[LOG] vehiclesWithData.size = ${vehiclesWithData.size}")
                val vehicles = vehiclesWithData.map { it.vehicle }
                Log.d("VehicleListViewModel", "[LOG] vehicles.size = ${vehicles.size}")
                val activeSessions = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.activeSessions.firstOrNull { 
                        it.sessionStartTime != null 
                    }
                }.filterValues { it != null }.mapValues { it.value!! }
                Log.d("VehicleListViewModel", "[LOG] activeSessions.size = ${activeSessions.size}")
                val lastChecks = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.lastPreShiftCheck?.let { checklistAnswer ->
                        app.forku.domain.model.checklist.PreShiftCheck(
                            id = checklistAnswer.id,
                            vehicleId = checklistAnswer.vehicleId ?: vehicleData.vehicle.id,
                            userId = checklistAnswer.goUserId,
                            status = checklistAnswer.status.toString(),
                            startDateTime = checklistAnswer.startDateTime,
                            endDateTime = checklistAnswer.endDateTime,
                            items = emptyList()
                        )
                    }
                }
                Log.d("VehicleListViewModel", "[LOG] lastChecks.size = ${lastChecks.size}")
                val checklistAnswers = vehiclesWithData.associate { vehicleData ->
                    vehicleData.vehicle.id to vehicleData.checklistAnswers
                        .sortedByDescending { 
                            it.lastCheckDateTime.takeIf { it.isNotBlank() } ?: it.endDateTime 
                        }
                        .firstOrNull()
                }.filterValues { it != null }.mapValues { it.value!! }
                Log.d("VehicleListViewModel", "[LOG] checklistAnswers.size = ${checklistAnswers.size}")

                _state.value = _state.value.copy(
                    vehicles = vehicles,
                    vehicleSessions = activeSessions,
                    lastPreShiftChecks = lastChecks,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
                _checklistAnswers.value = checklistAnswers
                Log.d("VehicleListViewModel", "[LOG] Estado final actualizado: vehicles=${vehicles.size}, sessions=${activeSessions.size}, lastChecks=${lastChecks.size}")

            } catch (e: Exception) {
                Log.e("VehicleListViewModel", "[LOG] Exception final: ${e.message}")
                _state.value = _state.value.copy(
                    error = "Error loading vehicles. Please try again later.",
                    isLoading = false,
                    isRefreshing = false
                )
            } finally {
                isLoading = false
                Log.d("VehicleListViewModel", "[LOG] FINALLY: isLoading=false")
            }
        }
    }
    
    /**
     * ✅ NEW: Set admin mode to control automatic context-based loading
     * When admin mode is true, prevents automatic reloading from BusinessContextManager
     * to avoid conflicts with filter-based loading
     */
    fun setAdminMode(enabled: Boolean) {
        Log.d("VehicleListViewModel", "🔧 Setting admin mode: $enabled")
        val wasAdminMode = isAdminMode
        isAdminMode = enabled
        
        // ✅ NEW: Reload sites when admin mode changes to ensure correct site list
        if (wasAdminMode != enabled) {
            Log.d("VehicleListViewModel", "🔄 Admin mode changed, reloading sites...")
            loadSites()
        }
    }
} 