package app.forku.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import coil.compose.AsyncImage
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.presentation.navigation.Screen
import app.forku.domain.model.user.UserRole
import app.forku.presentation.vehicle.list.VehicleItem
import app.forku.presentation.common.components.DashboardHeader
import app.forku.presentation.common.components.FeedbackBanner
import coil.ImageLoader
import app.forku.core.auth.TokenErrorHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.BusinessSiteFilterMode
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel
import androidx.navigation.NavHostController
import app.forku.presentation.dashboard.AdminDashboardViewModel
import app.forku.presentation.dashboard.DashboardState
import app.forku.presentation.dashboard.AdminDashboardState
import androidx.compose.ui.res.colorResource


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    imageLoader: ImageLoader,
    tokenErrorHandler: TokenErrorHandler
) {
    val navHostController = navController as? NavHostController
    val sharedFiltersViewModel: AdminSharedFiltersViewModel = if (navHostController?.currentBackStackEntry != null) {
        hiltViewModel(navHostController.currentBackStackEntry!!)
    } else {
        hiltViewModel()
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filterBusinessId by sharedFiltersViewModel.filterBusinessId.collectAsStateWithLifecycle()
    val filterSiteId by sharedFiltersViewModel.filterSiteId.collectAsStateWithLifecycle()
    val isAllSitesSelected by sharedFiltersViewModel.isAllSitesSelected.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Log.d("AdminDashboardScreen", "[FLOW] Composable loaded - currentUser: $currentUser, role: ${currentUser?.role}, filterBusinessId: $filterBusinessId, filterSiteId: $filterSiteId, isAllSitesSelected: $isAllSitesSelected")
    // ✅ SIMPLIFIED: Estado para saber si los filtros ya han sido inicializados desde DataStore
    var filtersInitialized by remember { mutableStateOf(false) }
    // Bandera para saber si ya se leyó cada filtro al menos una vez
    var businessIdRead by remember { mutableStateOf(false) }
    var siteIdRead by remember { mutableStateOf(false) }
    var allSitesRead by remember { mutableStateOf(false) }

    // Observa los cambios y marca como leído cuando cada filtro se inicializa
    LaunchedEffect(filterBusinessId) {
        Log.d("AdminDashboardScreen", "[FLOW] LaunchedEffect(filterBusinessId) - filterBusinessId: $filterBusinessId")
        if (!businessIdRead && filterBusinessId != null) businessIdRead = true
    }
    LaunchedEffect(filterSiteId) {
        Log.d("AdminDashboardScreen", "[FLOW] LaunchedEffect(filterSiteId) - filterSiteId: $filterSiteId")
        if (!siteIdRead) siteIdRead = true
    }
    LaunchedEffect(isAllSitesSelected) {
        Log.d("AdminDashboardScreen", "[FLOW] LaunchedEffect(isAllSitesSelected) - isAllSitesSelected: $isAllSitesSelected")
        if (!allSitesRead) allSitesRead = true
    }
    // Cuando los tres han sido leídos, marca filtersInitialized
    LaunchedEffect(businessIdRead, siteIdRead, allSitesRead) {
        Log.d("AdminDashboardScreen", "[FLOW] LaunchedEffect(businessIdRead, siteIdRead, allSitesRead) - businessIdRead: $businessIdRead, siteIdRead: $siteIdRead, allSitesRead: $allSitesRead")
        if (businessIdRead && siteIdRead && allSitesRead) filtersInitialized = true
    }
    
    // ✅ REMOVED: No more business context initialization - AdminSharedFiltersViewModel handles defaults
    
    // ✅ Load initial dashboard data for Admin users SOLO cuando los filtros estén listos
    LaunchedEffect(currentUser, filterBusinessId, filtersInitialized) {
        Log.d("AdminDashboardScreen", "[FLOW] LaunchedEffect(currentUser, filterBusinessId, filtersInitialized) - currentUser: $currentUser, role: ${currentUser?.role}, filterBusinessId: $filterBusinessId, filtersInitialized: $filtersInitialized")
        val isAdmin = currentUser?.role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
        if (filtersInitialized && isAdmin && filterBusinessId != null) {
            Log.d("AdminDashboardScreen", "[FLOW] ADMIN INITIAL LOAD: Loading dashboard data with filters (filtersReady=$filtersInitialized)")
            Log.d("AdminDashboardScreen", "  - businessId: $filterBusinessId")
            Log.d("AdminDashboardScreen", "  - isAllSitesSelected: $isAllSitesSelected")
            Log.d("AdminDashboardScreen", "  - filterSiteId: $filterSiteId")
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            viewModel.loadDashboardDataWithFilters(filterBusinessId!!, effectiveSiteId)
        }
    }
    
    // ✅ Monitor filter changes y recarga dashboard SOLO para admin y cuando los filtros estén listos
    LaunchedEffect(filterBusinessId, filterSiteId, isAllSitesSelected, filtersInitialized) {
        val isAdmin = currentUser?.role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
        if (filtersInitialized && isAdmin && filterBusinessId != null) {
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            Log.d("AdminDashboardScreen", "🔄 Filter change detected (filtersReady=$filtersInitialized):")
            Log.d("AdminDashboardScreen", "  - businessId: $filterBusinessId")
            Log.d("AdminDashboardScreen", "  - isAllSitesSelected: $isAllSitesSelected")
            Log.d("AdminDashboardScreen", "  - filterSiteId: $filterSiteId")
            Log.d("AdminDashboardScreen", "  - effectiveSiteId: $effectiveSiteId")
            viewModel.loadDashboardDataWithFilters(filterBusinessId!!, effectiveSiteId)
            viewModel.loadOperatingVehiclesCount(filterBusinessId, effectiveSiteId, isAllSitesSelected)
        }
    }
    
    // Enhanced logging for debugging operating vehicles count
    LaunchedEffect(state.operatingVehiclesCount, state.activeVehicleSessions.size) {
        Log.d("AdminDashboardScreen", "=== UI STATE UPDATE ===")
        Log.d("AdminDashboardScreen", "operatingVehiclesCount (from API): ${state.operatingVehiclesCount}")
        Log.d("AdminDashboardScreen", "activeVehicleSessions.size (from UI): ${state.activeVehicleSessions.size}")
        Log.d("AdminDashboardScreen", "userIncidentsCount: ${state.userIncidentsCount}")
        Log.d("AdminDashboardScreen", "totalIncidentsCount: ${state.totalIncidentsCount}")
        Log.d("AdminDashboardScreen", "safetyAlertsCount: ${state.safetyAlertsCount}")
        Log.d("AdminDashboardScreen", "isLoading: ${state.isLoading}")
        Log.d("AdminDashboardScreen", "error: ${state.error}")
        Log.d("AdminDashboardScreen", "========================")
    }
    val checklistAnswers by viewModel.checklistAnswers.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.authEvent.collect { event ->
            when (event) {
                AuthEvent.NavigateToLogin -> {
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

    // ✅ SIMPLIFIED: Forzar recarga de dashboard al volver a la pantalla
    LaunchedEffect(Unit) {
        val isAdmin = currentUser?.role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
        if (filterBusinessId != null && isAdmin) {
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            viewModel.loadDashboardDataWithFilters(filterBusinessId, effectiveSiteId)
        }
    }

    BaseScreen(
        navController = navController,
        showTopBar = false,
        showBottomBar = true,
        dashboardState = DashboardState(user = currentUser),
        topBarTitle = "Admin Dashboard",
        onAppResume = {},
        showLoadingOnRefresh = true,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(rememberPullRefreshState(
                    refreshing = state.isLoading,
                    onRefresh = { 
                        // ✅ FIXED: Admin always uses filters, never context
                        if (filterBusinessId != null) {
                            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
                            viewModel.loadDashboardDataWithFilters(filterBusinessId, effectiveSiteId)
                        }
                    }
                )),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { 
                        DashboardHeader(
                            userName = currentUser?.firstName ?: "",
                            onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                            onSettingsClick = { navController.navigate(Screen.SystemSettings.route) },
                            showNotifications = false // Temporarily hide notifications
                        )
                    }
                    
                    // ✅ FILTERS: Separate from user context - for data visualization only
                    item {
                        BusinessSiteFilters(
                            mode = BusinessSiteFilterMode.VIEW_FILTER,
                            currentUserRole = currentUser?.role,
                            selectedBusinessId = filterBusinessId,
                            selectedSiteId = if (isAllSitesSelected) null else filterSiteId,
                            isAllSitesSelected = isAllSitesSelected,
                            onBusinessChanged = { sharedFiltersViewModel.setBusinessId(it) },
                            onSiteChanged = { siteId ->
                                Log.d("AdminDashboardScreen", "🎯 Site selection changed: $siteId")
                                if (siteId == "ALL_SITES") {
                                    // "All Sites" selected - set site filter to null
                                    Log.d("AdminDashboardScreen", "🔧 Processing ALL_SITES selection")
                                    sharedFiltersViewModel.setSiteId(null)
                                    Log.d("AdminDashboardScreen", "✅ All Sites selected - filtering with null siteId")
                                } else {
                                    // Specific site selected
                                    Log.d("AdminDashboardScreen", "🔧 Processing specific site selection: $siteId")
                                    sharedFiltersViewModel.setSiteId(siteId)
                                    Log.d("AdminDashboardScreen", "✅ Specific site selected: $siteId")
                                }
                                // ✅ Data reload will be handled by LaunchedEffect observing filter changes
                            },
                            showBusinessFilter = false,
                            isCollapsible = true,
                            initiallyExpanded = false,
                            title = "View Filters",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            adminSharedFiltersViewModel = sharedFiltersViewModel
                        )
                    }
                    

                    
                    item { OperationStatusSection(state, navController) }
                    
                    // 🧪 TESTING: SessionKeepAlive Test Button
                    // item { SessionKeepAliveTestSection() }
                    
                    item { VehicleSessionSection(state, navController, imageLoader, checklistAnswers) }
                    
                    item { OperatorsInSessionSection(state, navController) }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        FeedbackBanner(
                            onFeedbackSubmitted = { rating, feedback, canContactMe ->
                                viewModel.submitFeedback(rating, feedback, canContactMe)
                            }
                        )
                        
                        // Show success message when feedback is submitted
                        if (state.feedbackSubmitted) {
                            Snackbar(
                                modifier = Modifier.padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Text("Thank you for your feedback!")
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isLoading,
                state = rememberPullRefreshState(
                    refreshing = state.isLoading,
                    onRefresh = { 
                        // ✅ FIXED: Admin always uses filters, never context
                        if (filterBusinessId != null) {
                            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
                            viewModel.loadDashboardDataWithFilters(filterBusinessId, effectiveSiteId)
                        }
                    }
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}



@Composable
private fun OperationStatusSection(
    state: AdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operation Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    icon = Icons.Default.DirectionsCar,
                    count = state.operatingVehiclesCount.toString(),
                    label = "Operating",
                    iconTint = Color(0xFF4CAF50),
                    onClick = { navController.navigate(Screen.VehiclesList.route) }
                )
                StatusItem(
                    icon = Icons.Default.Warning,
                    count = state.totalIncidentsCount.toString(),
                    label = "Incidents",
                    iconTint = Color(0xFFFFA726),
                    onClick = { navController.navigate(Screen.IncidentList.createRoute()) }
                )
                StatusItem(
                    icon = Icons.Default.Security,
                    count = state.safetyAlertsCount.toString(),
                    label = "Safety Alerts",
                    iconTint = Color(0xFF2196F3),
                    onClick = { navController.navigate(Screen.SafetyAlerts.route) }
                )
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun StatusItem(
    icon: ImageVector,
    count: String,
    label: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        color = Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = count,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun VehicleSessionSection(
    state: AdminDashboardState,
    navController: NavController,
    imageLoader: ImageLoader,
    checklistAnswers: Map<String, app.forku.domain.model.checklist.ChecklistAnswer>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicle In-Session",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate(Screen.VehiclesList.route) }
                ) {
                    Text(
                        "View all",
                        fontSize = 10.sp
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.activeVehicleSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active vehicle sessions",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.activeVehicleSessions.forEach { vehicleSessionInfo ->
                        VehicleItem(
                            vehicle = vehicleSessionInfo.vehicle,
                            userRole = UserRole.OPERATOR,
                            sessionInfo = vehicleSessionInfo,
                            showStatus = false,
                            lastPreShiftCheck = state.lastPreShiftChecks[vehicleSessionInfo.vehicle.id],
                            imageLoader = imageLoader,
                            checklistAnswer = checklistAnswers[vehicleSessionInfo.session.checkId],
                            onClick = {
                                navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleSessionInfo.vehicle.id))
                            }
                        )
                    }
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun OperatorsInSessionSection(
    state: AdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operators In-Session",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate(Screen.OperatorsList.route) }
                ) {
                    Text(
                        "View all",
                        fontSize = 10.sp
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.activeOperators.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No operators in session",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.activeOperators.forEach { operator ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            UserAvatar(
                                avatarData = getUserAvatarData(
                                    operator.name.split(" ").firstOrNull(),
                                    operator.name.split(" ").drop(1).firstOrNull(),
                                    operator.image
                                ),
                                size = 24.dp,
                                fontSize = 12.sp
                            )
                            Text(
                                text = operator.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}
