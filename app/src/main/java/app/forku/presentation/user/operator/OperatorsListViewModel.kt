package app.forku.presentation.user.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.user.UserBusinessRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.presentation.dashboard.OperatorSessionInfo
import app.forku.domain.model.user.UserRole
import app.forku.core.business.BusinessContextManager
import app.forku.core.auth.UserRoleManager
import app.forku.domain.repository.user.UserPreferencesRepository
import app.forku.presentation.common.components.BusinessContextUpdater
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlinx.coroutines.delay
import app.forku.core.Constants
import app.forku.domain.model.session.AdminDashboardData

@HiltViewModel
class OperatorsListViewModel @Inject constructor(
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository,
    private val userBusinessRepository: UserBusinessRepository,
    private val vehicleRepository: VehicleRepository,
    override val businessContextManager: BusinessContextManager,
    override val userPreferencesRepository: UserPreferencesRepository
) : ViewModel(), BusinessContextUpdater {

    private val _state = MutableStateFlow(OperatorsListState())
    val state = _state.asStateFlow()

    // Business context from BusinessContextManager
    val businessContextState = businessContextManager.contextState
    
    // ✅ Expose current user for UI to determine admin features
    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // ✅ NEW: Loading control to prevent duplicate requests
    private var isLoadingOperators = false
    private var isLoadingOperatorsWithFilters = false

    init {
        // ✅ FIXED: Only load current user and business context
        // Remove initial loadOperators() call to let UI control data loading based on filters and user role
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
            businessContextManager.loadBusinessContext()
            // ✅ REMOVED: loadOperators() - UI will handle loading based on filters and user role
        }
    }

    private fun createOperatorSessionInfo(
        user: app.forku.domain.model.user.User,
        activeSession: Boolean,
        sessionStartTime: String? = null
    ): OperatorSessionInfo {
        return OperatorSessionInfo(
            name = "${user.firstName} ${user.lastName}",
            fullName = user.fullName,
            username = user.username,
            image = user.photoUrl,
                    isActive = activeSession,
            userId = user.id,
                    sessionStartTime = sessionStartTime ?: "",
            role = user.role
                )
    }

    fun loadOperators(showLoading: Boolean = true) {
        // ✅ ADDED: Prevent duplicate requests
        if (isLoadingOperators) {
            android.util.Log.d("OperatorsList", "🔄 loadOperators: Already loading, skipping duplicate request")
            return
        }
        
        viewModelScope.launch {
            try {
                isLoadingOperators = true
                android.util.Log.d("OperatorsList", "=== 🚀 OPTIMIZED LOADING OPERATORS WITH BUSINESS FILTER ===")
                
                if (showLoading) {
                    _state.value = _state.value.copy(isLoading = true)
                }
                
                // Get business and site context from BusinessContextManager
                val businessId = businessContextManager.getCurrentBusinessId()
                val siteId = businessContextManager.getCurrentSiteId()
                android.util.Log.d("OperatorsList", "Business ID from context: '$businessId'")
                android.util.Log.d("OperatorsList", "Site ID from context: '$siteId'")
                android.util.Log.d("OperatorsList", "=== SITE FILTERING DEBUG ===")
                
                // 🚀 OPTIMIZATION: Use parallel API calls instead of sequential
                val deferredUsersWithSites = async { userRepository.getAllUsers(include = "UserSiteItems,UserBusinesses") }
                val deferredDashboardData = async { vehicleSessionRepository.getActiveSessionsWithRelatedData(businessId?:"", null) }
                
                // Wait for both API calls to complete
                val allUsersWithSites = deferredUsersWithSites.await()
                val dashboardData = deferredDashboardData.await()
                
                android.util.Log.d("OperatorsList", "✅ Parallel API calls completed")
                android.util.Log.d("OperatorsList", "Total users with site/business data: ${allUsersWithSites.size}")
                android.util.Log.d("OperatorsList", "Dashboard data - Sessions: ${dashboardData.activeSessions.size}, Operators: ${dashboardData.operators.size}")
                
                // 🚀 OPTIMIZATION: Get user-site mappings directly from API response
                val userSiteMappings = userRepository.getUserSiteMappings()
                android.util.Log.d("OperatorsList", "User-site mappings: $userSiteMappings")
                
                // 🚀 OPTIMIZATION: Process users with UserSiteItems and UserBusinesses from single API call
                val usersWithAssignments = allUsersWithSites.mapNotNull { user ->
                    android.util.Log.d("OperatorsList", "=== Processing User: ${user.fullName} (${user.id}) ===")
                    
                    // Check business assignments using existing businessId field
                    val hasBusinessMatch = user.businessId == businessId
                    android.util.Log.d("OperatorsList", "  - BusinessId from user.businessId: ${user.businessId} == $businessId: $hasBusinessMatch")
                    
                    // Site matching logic using UserSiteItems from API response:
                    // 1. If admin has no specific site (siteId == null), show all users in business
                    // 2. If admin has specific site, check if user has assignment to that specific site
                    val hasSiteMatch = if (siteId == null) {
                        // Admin has no site restriction, show all business users
                        android.util.Log.d("OperatorsList", "  - Admin has no site restriction, including all business users")
                        true
                    } else {
                        // Admin has specific site, check if user is assigned to that site using real API data
                        val userSiteIds = userSiteMappings[user.id] ?: emptyList()
                        val userHasCorrectSite = userSiteIds.contains(siteId)
                        
                        android.util.Log.d("OperatorsList", "  - User ${user.fullName} site assignments: $userSiteIds")
                        android.util.Log.d("OperatorsList", "  - Looking for siteId: $siteId")
                        android.util.Log.d("OperatorsList", "  - User has correct site: $userHasCorrectSite")
                        
                        userHasCorrectSite
                    }
                    
                    android.util.Log.d("OperatorsList", "  - SiteId match: $hasSiteMatch (admin siteId: $siteId)")
                    
                    if (hasBusinessMatch && hasSiteMatch) {
                        android.util.Log.d("OperatorsList", "  ✅ User included in results")
                        user
                    } else {
                        android.util.Log.d("OperatorsList", "  ❌ User excluded from results (businessMatch: $hasBusinessMatch, siteMatch: $hasSiteMatch)")
                        null
                    }
                }
                
                android.util.Log.d("OperatorsList", "Users matching business and site context: ${usersWithAssignments.size}")
                
                // Create a map of user roles (using user.role from domain model)
                val businessUserRoles = usersWithAssignments.associateBy({ it.id }, { it.role.name })
                
                val usersInBusinessAndSite = usersWithAssignments.map { it.id }
                android.util.Log.d("OperatorsList", "Users assigned to business '$businessId' and site '$siteId': ${usersInBusinessAndSite.size}")
                android.util.Log.d("OperatorsList", "User IDs in business and site: $usersInBusinessAndSite")
                android.util.Log.d("OperatorsList", "Business user roles: $businessUserRoles")
                
                // Log filtered users details
                android.util.Log.d("OperatorsList", "=== FILTERED USERS ===")
                usersWithAssignments.forEach { user ->
                    android.util.Log.d("OperatorsList", "✅ Included: userId=${user.id}, fullName=${user.fullName}, businessId=${user.businessId}, role=${user.role}")
                }
                
                // 🚀 OPTIMIZATION: Use operators from optimized dashboard data instead of getAllUsers()
                val allUsers = dashboardData.operators.values.toList()
                android.util.Log.d("OperatorsList", "Total users from optimized data: ${allUsers.size}")
                
                // Add any missing users that are assigned to business and site but not in active sessions
                val activeUserIds = allUsers.map { it.id }.toSet()
                val missingUserIds = usersInBusinessAndSite.filter { !activeUserIds.contains(it) }
                
                if (missingUserIds.isNotEmpty()) {
                    android.util.Log.d("OperatorsList", "Found ${missingUserIds.size} users assigned to business but not in active sessions")
                    // For missing users, we need to fetch them separately (this is the edge case)
                    val missingUsers = missingUserIds.mapNotNull { userId ->
                        try {
                            userRepository.getUserById(userId)
                            } catch (e: Exception) {
                            android.util.Log.e("OperatorsList", "Error fetching missing user $userId", e)
                                null
                            }
                    }
                    android.util.Log.d("OperatorsList", "Fetched ${missingUsers.size} missing users")
                    val completeUserList = allUsers + missingUsers
                    
                    // Log details of all users
                    completeUserList.forEach { user ->
                        android.util.Log.d("OperatorsList", "Complete users - User: ${user.fullName} (${user.id}), Role: ${user.role}, BusinessId: ${user.businessId}")
                    }
                    
                    // Filter users by business and site assignment and update their roles using UserRoleManager
                    val businessUsers = completeUserList.filter { user ->
                        usersInBusinessAndSite.contains(user.id)
                    }.map { user ->
                        // Use UserRoleManager to determine the effective role
                        val businessRole = businessUserRoles[user.id]?.let { 
                            UserRoleManager.fromString(it) 
                        }
                        
                        // Get UserRoleItems from the user if available (from optimized data)
                        val userRoleItems = if (user.id in dashboardData.operators) {
                            // Try to get UserRoleItems from the session data if available
                            null // We'll enhance this later when we have access to UserRoleItems in the optimized data
                        } else null
                        
                        val finalRole = UserRoleManager.getEffectiveRole(
                            user = user,
                            userRoleItems = userRoleItems,
                            businessRole = businessRole,
                            businessId = businessId
                        )
                        
                        android.util.Log.d("OperatorsList", "User ${user.fullName}: Effective role determined as $finalRole (general: ${user.role}, business: $businessRole)")
                        
                        // Create a new user instance with the correct role for this business
                        user.copy(role = finalRole)
                    }
                    
                    android.util.Log.d("OperatorsList", "Users filtered by business context: ${businessUsers.size}")
                    businessUsers.forEach { user ->
                        android.util.Log.d("OperatorsList", "  - Filtered User: ${user.fullName} (${user.id}), Final Role: ${user.role}, BusinessId: ${user.businessId}")
                    }
                    
                    // 🚀 OPTIMIZATION: Use sessions from optimized dashboard data (no additional API calls!)
                    val activeSessions = dashboardData.activeSessions
                    android.util.Log.d("OperatorsList", "✅ Using ${activeSessions.size} active sessions from optimized data")
                    
                    // Create a map of operator IDs to their active sessions (no API calls needed!)
                    val activeOperatorIds = activeSessions.associate { session -> 
                        session.userId to session.startTime 
                    }
                    android.util.Log.d("OperatorsList", "Active operators in sessions: ${activeOperatorIds.keys}")
                    
                    // Process business users and mark them as active/inactive using optimized data
                    val userInfos = businessUsers.map { user ->
                        val activeSessionStartTime = activeOperatorIds[user.id]
                        android.util.Log.d("OperatorsList", "=== Processing Business User ===")
                        android.util.Log.d("OperatorsList", "User ID: ${user.id}")
                        android.util.Log.d("OperatorsList", "User fullName: ${user.fullName}")
                        android.util.Log.d("OperatorsList", "User email: ${user.email}")
                        android.util.Log.d("OperatorsList", "User username: ${user.username}")
                        android.util.Log.d("OperatorsList", "User role: ${user.role}")
                        android.util.Log.d("OperatorsList", "Active session: ${activeSessionStartTime != null}")
                        
                        val operatorInfo = createOperatorSessionInfo(
                            user = user,
                            activeSession = activeSessionStartTime != null,
                            sessionStartTime = activeSessionStartTime
                        )
                        
                        android.util.Log.d("OperatorsList", "Created OperatorSessionInfo with role: ${operatorInfo.role}")
                        operatorInfo
                    }.sortedWith(
                        compareByDescending<OperatorSessionInfo> { it.isActive }
                        .thenByDescending { it.sessionStartTime }
                    )

                    android.util.Log.d("OperatorsList", "=== 🎉 OPTIMIZED RESULT ===")
                    android.util.Log.d("OperatorsList", "Total operators to display: ${userInfos.size}")
                    android.util.Log.d("OperatorsList", "Active operators: ${userInfos.count { it.isActive }}")
                    android.util.Log.d("OperatorsList", "🚀 Performance: 2 API calls (+ ${missingUserIds.size} individual fetches) vs ${usersInBusinessAndSite.size + 3} traditional calls")
                    
                    // Log final users being displayed
                    android.util.Log.d("OperatorsList", "=== FINAL USERS DISPLAYED ===")
                    userInfos.forEach { userInfo ->
                        android.util.Log.d("OperatorsList", "👤 User: ${userInfo.name} (${userInfo.userId}), Role: ${userInfo.role}, Active: ${userInfo.isActive}")
                    }
                    android.util.Log.d("OperatorsList", "========================")

                    _state.value = _state.value.copy(
                        operators = userInfos,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        currentBusinessId = businessId,
                        currentSiteId = siteId,
                        hasBusinessContext = businessContextManager.hasRealBusinessContext()
                    )
                } else {
                    // Happy path: all assigned users are in active sessions
                    android.util.Log.d("OperatorsList", "✅ All assigned users are covered by optimized dashboard data")
                    
                    // Log details of all users
                    allUsers.forEach { user ->
                        android.util.Log.d("OperatorsList", "Optimized users - User: ${user.fullName} (${user.id}), Role: ${user.role}, BusinessId: ${user.businessId}")
                    }
                    
                    // Filter users by business and site assignment and update their roles using UserRoleManager
                    val businessUsers = allUsers.filter { user ->
                        usersInBusinessAndSite.contains(user.id)
                    }.map { user ->
                        // Use UserRoleManager to determine the effective role
                        val businessRole = businessUserRoles[user.id]?.let { 
                            UserRoleManager.fromString(it) 
                        }
                        
                        // Get UserRoleItems from the user if available (from optimized data)
                        val userRoleItems = if (user.id in dashboardData.operators) {
                            // Try to get UserRoleItems from the session data if available
                            null // We'll enhance this later when we have access to UserRoleItems in the optimized data
                        } else null
                        
                        val finalRole = UserRoleManager.getEffectiveRole(
                            user = user,
                            userRoleItems = userRoleItems,
                            businessRole = businessRole,
                            businessId = businessId
                        )
                        
                        android.util.Log.d("OperatorsList", "User ${user.fullName}: Effective role determined as $finalRole (general: ${user.role}, business: $businessRole)")
                        
                        // Create a new user instance with the correct role for this business
                        user.copy(role = finalRole)
                    }
                    
                    android.util.Log.d("OperatorsList", "Users filtered by business context: ${businessUsers.size}")
                    businessUsers.forEach { user ->
                        android.util.Log.d("OperatorsList", "  - Filtered User: ${user.fullName} (${user.id}), Final Role: ${user.role}, BusinessId: ${user.businessId}")
                    }
                    
                    // 🚀 OPTIMIZATION: Use sessions from optimized dashboard data (no additional API calls!)
                    val activeSessions = dashboardData.activeSessions
                    android.util.Log.d("OperatorsList", "✅ Using ${activeSessions.size} active sessions from optimized data")
                    
                    // Create a map of operator IDs to their active sessions (no API calls needed!)
                    val activeOperatorIds = activeSessions.associate { session -> 
                        session.userId to session.startTime 
                    }
                    android.util.Log.d("OperatorsList", "Active operators in sessions: ${activeOperatorIds.keys}")
                    
                    // Process business users and mark them as active/inactive using optimized data
                    val userInfos = businessUsers.map { user ->
                        val activeSessionStartTime = activeOperatorIds[user.id]
                        android.util.Log.d("OperatorsList", "=== Processing Business User ===")
                        android.util.Log.d("OperatorsList", "User ID: ${user.id}")
                        android.util.Log.d("OperatorsList", "User fullName: ${user.fullName}")
                        android.util.Log.d("OperatorsList", "User email: ${user.email}")
                        android.util.Log.d("OperatorsList", "User username: ${user.username}")
                        android.util.Log.d("OperatorsList", "User role: ${user.role}")
                        android.util.Log.d("OperatorsList", "Active session: ${activeSessionStartTime != null}")
                        
                        val operatorInfo = createOperatorSessionInfo(
                            user = user,
                            activeSession = activeSessionStartTime != null,
                            sessionStartTime = activeSessionStartTime
                        )
                        
                        android.util.Log.d("OperatorsList", "Created OperatorSessionInfo with role: ${operatorInfo.role}")
                        operatorInfo
                }.sortedWith(
                    compareByDescending<OperatorSessionInfo> { it.isActive }
                    .thenByDescending { it.sessionStartTime }
                )

                    android.util.Log.d("OperatorsList", "=== 🎉 OPTIMIZED RESULT ===")
                    android.util.Log.d("OperatorsList", "Total operators to display: ${userInfos.size}")
                    android.util.Log.d("OperatorsList", "Active operators: ${userInfos.count { it.isActive }}")
                    android.util.Log.d("OperatorsList", "🚀 Performance: 2 parallel API calls vs ${usersInBusinessAndSite.size + 3} traditional calls")
                    
                    // Log final users being displayed
                    android.util.Log.d("OperatorsList", "=== FINAL USERS DISPLAYED ===")
                    userInfos.forEach { userInfo ->
                        android.util.Log.d("OperatorsList", "👤 User: ${userInfo.name} (${userInfo.userId}), Role: ${userInfo.role}, Active: ${userInfo.isActive}")
                    }
                    android.util.Log.d("OperatorsList", "========================")

                _state.value = _state.value.copy(
                    operators = userInfos,
                    isLoading = false,
                    isRefreshing = false,
                        error = null,
                        currentBusinessId = businessId,
                        currentSiteId = siteId,
                        hasBusinessContext = businessContextManager.hasRealBusinessContext()
                )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("OperatorsList", "Error loading operators", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Error loading operators: ${e.message}"
                )
            } finally {
                // ✅ ADDED: Always reset loading flag
                isLoadingOperators = false
            }
        }
    }

    fun refresh() {
        loadOperators(showLoading = false)
    }

    fun refreshWithLoading() {
        loadOperators(showLoading = true)
    }
    
    /**
     * Refresh business context and reload operators
     * Useful when user switches business or business assignment changes
     */
    fun refreshBusinessContext() {
        viewModelScope.launch {
            try {
                android.util.Log.d("OperatorsList", "Refreshing business context...")
                
                // Use BusinessContextManager to refresh context
                businessContextManager.refreshBusinessContext()
                
                // Reload operators with new context
                loadOperators(showLoading = true)
                
            } catch (e: Exception) {
                android.util.Log.e("OperatorsList", "Error refreshing business context: ${e.message}", e)
                _state.value = _state.value.copy(
                    error = "Failed to refresh business context: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Implementation of BusinessContextUpdater interface
     * Reloads operators when context changes
     */
    override fun reloadData() {
        loadOperators(showLoading = true)
    }
    
    /**
     * ✅ NEW: Load operators with temporary filters (VIEW_FILTER mode)
     * Does NOT change user's personal context/preferences
     * Used for admin filtering across different sites
     */
    fun loadOperatorsWithFilters(filterBusinessId: String?, filterSiteId: String?) {
        // ✅ ADDED: Prevent duplicate requests
        if (isLoadingOperatorsWithFilters) {
            android.util.Log.d("OperatorsList", "🔄 loadOperatorsWithFilters: Already loading, skipping duplicate request")
            return
        }
        
        viewModelScope.launch {
            try {
                isLoadingOperatorsWithFilters = true
                android.util.Log.d("OperatorsList", "=== 🚀 VIEW_FILTER: LOADING OPERATORS WITH FILTERS (UserPreferences) ===")
                _state.value = _state.value.copy(isLoading = true)
                
                val businessId = filterBusinessId ?: businessContextManager.getCurrentBusinessId()
                val siteId = filterSiteId
                android.util.Log.d("OperatorsList", "🔧 VIEW_FILTER: Loading operators with filters - businessId: $businessId, siteId: $siteId (null = All Sites)")
                
                if (businessId == null) {
                    android.util.Log.w("OperatorsList", "🔧 VIEW_FILTER: No business ID available, cannot load operators")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "No business context available"
                    )
                    return@launch
                }
                
                // Fetch all users (with UserSiteItems, UserBusinesses)
                val allUsersWithSites = userRepository.getAllUsers(include = "UserSiteItems,UserBusinesses")
                
                // Fetch UserPreferences for all users in parallel
                val userPreferencesMap = allUsersWithSites.associate { user ->
                    user.id to kotlin.runCatching { userPreferencesRepository.getUserPreferences(user.id) }.getOrNull()
                }
                
                // Filtering logic: use UserPreferences for businessId and siteId if available
                val usersWithAssignments = allUsersWithSites.filter { user ->
                    val prefs = userPreferencesMap[user.id]
                    val userBusinessId = prefs?.getEffectiveBusinessId() ?: user.businessId
                    val userSiteId = prefs?.getEffectiveSiteId() ?: user.siteId
                    val businessMatch = userBusinessId == businessId
                    val siteMatch = if (siteId == null) true else userSiteId == siteId
                    businessMatch && siteMatch
                }
                
                android.util.Log.d("OperatorsList", "Users matching business and site filters (UserPreferences): ${usersWithAssignments.size}")
                
                // Create a map of user roles (using user.role from domain model)
                val businessUserRoles = usersWithAssignments.associateBy({ it.id }, { it.role.name })
                val usersInBusinessAndSite = usersWithAssignments.map { it.id }
                
                // Use dashboard data for active sessions
                val dashboardData = vehicleSessionRepository.getActiveSessionsWithRelatedData(businessId, siteId)
                val allUsers = dashboardData.operators.values.toList()
                val activeUserIds = allUsers.map { it.id }.toSet()
                val missingUserIds = usersInBusinessAndSite.filter { !activeUserIds.contains(it) }
                
                val missingUsers = missingUserIds.mapNotNull { userId ->
                    try { userRepository.getUserById(userId) } catch (_: Exception) { null }
                }
                
                val completeUserList = allUsers + missingUsers
                val userInfos = processUsersForDisplay(completeUserList, usersInBusinessAndSite, businessUserRoles, businessId, dashboardData)
                
                _state.value = _state.value.copy(
                    operators = userInfos,
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                    currentBusinessId = businessId,
                    currentSiteId = siteId,
                    hasBusinessContext = true
                )
                
            } catch (e: Exception) {
                android.util.Log.e("OperatorsList", "🔧 VIEW_FILTER: Error loading operators with filters (UserPreferences)", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Error loading operators: ${e.message}"
                )
            } finally {
                // ✅ ADDED: Always reset loading flag
                isLoadingOperatorsWithFilters = false
            }
        }
    }
    
    /**
     * Helper function to process users and create operator info
     */
    private fun processUsersForDisplay(
        allUsers: List<app.forku.domain.model.user.User>,
        usersInBusinessAndSite: List<String>,
        businessUserRoles: Map<String, String>,
        businessId: String,
        dashboardData: AdminDashboardData
    ): List<OperatorSessionInfo> {
        // Filter users by business and site assignment and update their roles using UserRoleManager
        val businessUsers = allUsers.filter { user ->
            usersInBusinessAndSite.contains(user.id)
        }.map { user ->
            // Use UserRoleManager to determine the effective role
            val businessRole = businessUserRoles[user.id]?.let { 
                UserRoleManager.fromString(it) 
            }
            
            // Get UserRoleItems from the user if available (from optimized data)
            val userRoleItems = if (user.id in dashboardData.operators) {
                // Try to get UserRoleItems from the session data if available
                null // We'll enhance this later when we have access to UserRoleItems in the optimized data
            } else null
            
            val finalRole = UserRoleManager.getEffectiveRole(
                user = user,
                userRoleItems = userRoleItems,
                businessRole = businessRole,
                businessId = businessId
            )
            
            android.util.Log.d("OperatorsList", "User ${user.fullName}: Effective role determined as $finalRole (general: ${user.role}, business: $businessRole)")
            
            // Create a new user instance with the correct role for this business
            user.copy(role = finalRole)
        }
        
        android.util.Log.d("OperatorsList", "Users filtered by business context: ${businessUsers.size}")
        businessUsers.forEach { user ->
            android.util.Log.d("OperatorsList", "  - Filtered User: ${user.fullName} (${user.id}), Final Role: ${user.role}, BusinessId: ${user.businessId}")
        }
        
        // 🚀 OPTIMIZATION: Use sessions from optimized dashboard data (no additional API calls!)
        val activeSessions = dashboardData.activeSessions
        android.util.Log.d("OperatorsList", "✅ Using ${activeSessions.size} active sessions from optimized data")
        
        // Create a map of operator IDs to their active sessions (no API calls needed!)
        val activeOperatorIds = activeSessions.associate { session -> 
            session.userId to session.startTime 
        }
        android.util.Log.d("OperatorsList", "Active operators in sessions: ${activeOperatorIds.keys}")
        
        // Process business users and mark them as active/inactive using optimized data
        return businessUsers.map { user ->
            val activeSessionStartTime = activeOperatorIds[user.id]
            android.util.Log.d("OperatorsList", "=== Processing Business User ===")
            android.util.Log.d("OperatorsList", "User ID: ${user.id}")
            android.util.Log.d("OperatorsList", "User fullName: ${user.fullName}")
            android.util.Log.d("OperatorsList", "User email: ${user.email}")
            android.util.Log.d("OperatorsList", "User username: ${user.username}")
            android.util.Log.d("OperatorsList", "User role: ${user.role}")
            android.util.Log.d("OperatorsList", "Active session: ${activeSessionStartTime != null}")
            
            val operatorInfo = createOperatorSessionInfo(
                user = user,
                activeSession = activeSessionStartTime != null,
                sessionStartTime = activeSessionStartTime
            )
            
            android.util.Log.d("OperatorsList", "Created OperatorSessionInfo with role: ${operatorInfo.role}")
            operatorInfo
        }.sortedWith(
            compareByDescending<OperatorSessionInfo> { it.isActive }
            .thenByDescending { it.sessionStartTime }
        )
    }
} 