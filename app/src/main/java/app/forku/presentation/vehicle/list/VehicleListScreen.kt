package app.forku.presentation.vehicle.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.ErrorScreen
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.mutableStateOf
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.Vehicle
import coil.ImageLoader
import androidx.compose.runtime.setValue
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.BusinessSiteFilterMode
import android.util.Log
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VehicleListScreen(
    navController: NavController,
    viewModel: VehicleListViewModel = hiltViewModel(),
    onVehicleClick: (Vehicle) -> Unit,
    networkManager: NetworkConnectivityManager,
    userRole: UserRole = UserRole.OPERATOR,
    imageLoader: ImageLoader,
    tokenErrorHandler: TokenErrorHandler
) {
    val navHostController = navController as? NavHostController
    val owner = LocalViewModelStoreOwner.current
    val sharedFiltersViewModel: AdminSharedFiltersViewModel = hiltViewModel(viewModelStoreOwner = owner!!)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val checklistAnswers by viewModel.checklistAnswers.collectAsStateWithLifecycle()
    val filterBusinessId by sharedFiltersViewModel.filterBusinessId.collectAsStateWithLifecycle()
    val filterSiteId by sharedFiltersViewModel.filterSiteId.collectAsStateWithLifecycle()
    val isAllSitesSelected by sharedFiltersViewModel.isAllSitesSelected.collectAsStateWithLifecycle()
    var isUserFilterAction by remember { mutableStateOf(false) }

    val businessContextState by viewModel.businessContextManager.contextState.collectAsStateWithLifecycle()

    // ✅ SIMPLIFIED: Single LaunchedEffect for vehicle loading - handles both admin and operator
    LaunchedEffect(currentUser, filterBusinessId, filterSiteId, isAllSitesSelected) {
        Log.d("VehicleListScreen", "=== VEHICLE LOAD TRIGGERED ===")
        Log.d("VehicleListScreen", "currentUser: ${currentUser?.id}, role: ${currentUser?.role}")
        Log.d("VehicleListScreen", "filterBusinessId: $filterBusinessId, filterSiteId: $filterSiteId, isAllSitesSelected: $isAllSitesSelected")
        Log.d("VehicleListScreen", "businessContextState.businessId: ${businessContextState.businessId}, businessContextState.siteId: ${businessContextState.siteId}")
        val isAdmin = currentUser?.role in listOf(
            UserRole.ADMIN,
            UserRole.SUPERADMIN,
            UserRole.SYSTEM_OWNER
        )
        Log.d("VehicleListScreen", "isAdmin: $isAdmin")
        
        if (isAdmin) {
            // Admin mode: use filters when ready
            if (filterBusinessId != null) {
                val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
                Log.d("VehicleListScreen", "🎯 ADMIN LOAD: Loading vehicles with filters")
                Log.d("VehicleListScreen", "  - businessId: $filterBusinessId")
                Log.d("VehicleListScreen", "  - isAllSitesSelected: $isAllSitesSelected")
                Log.d("VehicleListScreen", "  - filterSiteId: $filterSiteId")
                Log.d("VehicleListScreen", "  - effectiveSiteId: $effectiveSiteId")
                // ✅ CRITICAL: Set admin mode FIRST to prevent context-based loading
                viewModel.setAdminMode(true)
                // Small delay to ensure admin mode is set before loading
                kotlinx.coroutines.delay(50)
                viewModel.loadVehiclesWithFilters(filterBusinessId, effectiveSiteId, isAllSitesSelected)
            }
        } else {
            // Operator mode: use context-based loading
            Log.d("VehicleListScreen", "👤 OPERATOR LOAD: Loading vehicles with context")
            viewModel.setAdminMode(false)
            viewModel.loadVehicles(true)
        }
    }
    
    // ✅ CENTRALIZED: Single refresh function to avoid multiple triggers
    fun handleRefresh() {
        val isAdmin = currentUser?.role in listOf(
            UserRole.ADMIN,
            UserRole.SUPERADMIN,
            UserRole.SYSTEM_OWNER
        )
        
        if (isAdmin) {
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            Log.d("VehicleListScreen", "🔄 CENTRALIZED REFRESH: Admin loading with filters")
            Log.d("VehicleListScreen", "  - filterBusinessId: $filterBusinessId")
            Log.d("VehicleListScreen", "  - effectiveSiteId: $effectiveSiteId (null = All Sites)")
            viewModel.loadVehiclesWithFilters(filterBusinessId ?: "", effectiveSiteId, isAllSitesSelected)
        } else {
            Log.d("VehicleListScreen", "🔄 CENTRALIZED REFRESH: Operator loading with context")
            viewModel.loadVehicles(true)
        }
    }
    
    // ✅ FIXED: Use correct loading method based on admin mode
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { handleRefresh() }
    )

    // Sort vehicles based on active sessions and current user's vehicle
    val sortedVehicles = remember(state.vehicles, state.vehicleSessions, currentUser) {
        state.vehicles.sortedWith(
            compareByDescending<Vehicle> { vehicle ->
                // First priority: Current user's active vehicle (if operator)
                val isCurrentUserVehicle = if (userRole == UserRole.OPERATOR) {
                    val session = state.vehicleSessions[vehicle.id]
                    session?.operator?.id == currentUser?.id && session?.sessionStartTime != null
                } else false
                
                // Return a number to ensure proper ordering
                when {
                    isCurrentUserVehicle -> 2 // Highest priority
                    state.vehicleSessions[vehicle.id]?.sessionStartTime != null -> 1 // Second priority
                    else -> 0 // Lowest priority
                }
            }.thenBy { vehicle ->
                // Final priority: Vehicle codename for consistent ordering
                vehicle.codename
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.authEvent.collect { event ->
            when (event) {
                is VehicleListViewModel.AuthEvent.NavigateToLogin -> {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }

    // ✅ ELIMINATED: Redundant LaunchedEffect for user role - now handled in centralized loading
    // ✅ ELIMINATED: Redundant LaunchedEffect for initial loading - now handled in centralized loading

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Vehicles",
        showBottomBar = false,
        onAppResume = null, // ✅ DISABLED: Avoid automatic refresh conflicts
        showLoadingOnRefresh = false,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ✅ DEBUG: Log values being passed to BusinessSiteFilters
                Log.d("VehicleListScreen", "🔍 RENDER BusinessSiteFilters with:")
                Log.d("VehicleListScreen", "  - selectedBusinessId: $filterBusinessId")
                Log.d("VehicleListScreen", "  - selectedSiteId: $filterSiteId")
                Log.d("VehicleListScreen", "  - isAllSitesSelected: $isAllSitesSelected")
                Log.d("VehicleListScreen", "  - currentUserRole: ${currentUser?.role}")
                
                // Business and Site Filters (Centralized Component)
                if (currentUser?.role in listOf(
                    UserRole.ADMIN,
                    UserRole.SUPERADMIN,
                    UserRole.SYSTEM_OWNER
                )) {
                    BusinessSiteFilters(
                        mode = BusinessSiteFilterMode.VIEW_FILTER,
                        currentUserRole = currentUser?.role,
                        selectedBusinessId = filterBusinessId,
                        selectedSiteId = filterSiteId,
                        isAllSitesSelected = isAllSitesSelected,
                        onBusinessChanged = { sharedFiltersViewModel.setBusinessId(it); isUserFilterAction = true },
                        onSiteChanged = { siteId ->
                            Log.d("VehicleListScreen", "🎯 Site selection changed: $siteId")
                            if (siteId == "ALL_SITES") {
                                // "All Sites" selected - set site filter to null
                                Log.d("VehicleListScreen", "🔧 Processing ALL_SITES selection")
                                sharedFiltersViewModel.setSiteId(null)
                                Log.d("VehicleListScreen", "✅ All Sites selected - filtering with null siteId")
                            } else {
                                // Specific site selected
                                Log.d("VehicleListScreen", "🔧 Processing specific site selection: $siteId")
                                sharedFiltersViewModel.setSiteId(siteId)
                                Log.d("VehicleListScreen", "✅ Specific site selected: $siteId")
                            }
                            isUserFilterAction = true
                        },
                        showBusinessFilter = false,
                        isCollapsible = true,
                        initiallyExpanded = false,
                        title = "Vehicle Filters",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        adminSharedFiltersViewModel = sharedFiltersViewModel
                    )
                }
                
                // Old site filter removed - now handled by centralized BusinessSiteFilters component
                when {
                    state.error != null -> ErrorScreen(
                        message = state.error!!,
                        onRetry = {
                            val isAdmin = currentUser?.role in listOf(
                                UserRole.ADMIN,
                                UserRole.SUPERADMIN,
                                UserRole.SYSTEM_OWNER
                            )
                            
                            if (isAdmin) {
                                // ✅ FIXED: Admin always uses filter-based loading, fallback to context if filters not ready
                                val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
                                Log.d("VehicleListScreen", "🔄 ERROR RETRY: Admin loading with filters")
                                Log.d("VehicleListScreen", "  - filterBusinessId: $filterBusinessId")
                                Log.d("VehicleListScreen", "  - effectiveSiteId: $effectiveSiteId (null = All Sites)")
                                viewModel.loadVehiclesWithFilters(filterBusinessId ?: "", effectiveSiteId, isAllSitesSelected)
                            } else {
                                // Operator mode - use context-based loading
                                Log.d("VehicleListScreen", "🔄 ERROR RETRY: Operator loading with context")
                                viewModel.loadVehicles(true)
                            }
                        }
                    )
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .widthIn(max = 800.dp)
                                    .fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sortedVehicles) { vehicle ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Box(modifier = Modifier.padding(12.dp)) {
                                            VehicleItem(
                                                vehicle = vehicle,
                                                userRole = userRole,
                                                sessionInfo = state.vehicleSessions[vehicle.id],
                                                lastPreShiftCheck = state.lastPreShiftChecks[vehicle.id],
                                                imageLoader = imageLoader,
                                                checklistAnswer = checklistAnswers[vehicle.id],
                                                onClick = { onVehicleClick(vehicle) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Only show loading indicator for pull-to-refresh
            if (state.isLoading && state.isRefreshing) {
                PullRefreshIndicator(
                    refreshing = true,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
} 