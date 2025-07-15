package app.forku.presentation.incident.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanFromMillis
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.IncidentCard
import app.forku.domain.model.incident.IncidentStatus
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.BusinessSiteFilterMode
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel
import androidx.navigation.NavHostController
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun IncidentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    userId: String? = null,
    source: String? = null,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: IncidentListViewModel = hiltViewModel()
) {
    val navHostController = navController as? NavHostController
    val sharedFiltersViewModel: AdminSharedFiltersViewModel = if (navHostController?.currentBackStackEntry != null) {
        hiltViewModel(navHostController.currentBackStackEntry!!)
    } else {
        hiltViewModel()
    }
    
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val filterBusinessId by sharedFiltersViewModel.filterBusinessId.collectAsStateWithLifecycle()
    val filterSiteId by sharedFiltersViewModel.filterSiteId.collectAsStateWithLifecycle()
    val isAllSitesSelected by sharedFiltersViewModel.isAllSitesSelected.collectAsStateWithLifecycle()

    // ✅ SIMPLIFIED: Single LaunchedEffect that handles both admin and operator loading
    LaunchedEffect(currentUser, filterBusinessId, filterSiteId, isAllSitesSelected, userId, source) {
        val isAdmin = currentUser?.role in listOf(
            app.forku.domain.model.user.UserRole.ADMIN,
            app.forku.domain.model.user.UserRole.SUPERADMIN,
            app.forku.domain.model.user.UserRole.SYSTEM_OWNER
        )
        
        Log.d("IncidentListScreen", "=== INCIDENT LOAD TRIGGERED ===")
        Log.d("IncidentListScreen", "currentUser: ${currentUser?.id}, role: ${currentUser?.role}")
        Log.d("IncidentListScreen", "isAdmin: $isAdmin")
        Log.d("IncidentListScreen", "userId: $userId, source: $source")
        Log.d("IncidentListScreen", "filterBusinessId: $filterBusinessId, filterSiteId: $filterSiteId, isAllSitesSelected: $isAllSitesSelected")
        
        if (isAdmin) {
            // Admin mode: use filters when ready
            if (filterBusinessId != null) {
                val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
                Log.d("IncidentListScreen", "🎯 ADMIN LOAD: Loading incidents with filters")
                Log.d("IncidentListScreen", "  - businessId: $filterBusinessId")
                Log.d("IncidentListScreen", "  - isAllSitesSelected: $isAllSitesSelected")
                Log.d("IncidentListScreen", "  - filterSiteId: $filterSiteId")
                Log.d("IncidentListScreen", "  - effectiveSiteId: $effectiveSiteId")
                viewModel.loadIncidentsWithFilters(filterBusinessId, effectiveSiteId)
            }
        } else {
            // Operator mode: use context-based loading
            Log.d("IncidentListScreen", "👤 OPERATOR LOAD: Loading incidents with context")
            Log.d("IncidentListScreen", "  - userId: $userId")
            Log.d("IncidentListScreen", "  - source: $source")
            viewModel.loadIncidents(userId, source)
        }
    }

    // ✅ CENTRALIZED: Single refresh function to avoid multiple triggers
    fun handleRefresh() {
        val isAdmin = currentUser?.role in listOf(
            app.forku.domain.model.user.UserRole.ADMIN,
            app.forku.domain.model.user.UserRole.SUPERADMIN,
            app.forku.domain.model.user.UserRole.SYSTEM_OWNER
        )
        
        if (isAdmin) {
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            Log.d("IncidentListScreen", "🔄 CENTRALIZED REFRESH: Admin loading with filters")
            Log.d("IncidentListScreen", "  - filterBusinessId: $filterBusinessId")
            Log.d("IncidentListScreen", "  - effectiveSiteId: $effectiveSiteId (null = All Sites)")
            viewModel.loadIncidentsWithFilters(filterBusinessId, effectiveSiteId)
        } else {
            Log.d("IncidentListScreen", "🔄 CENTRALIZED REFRESH: Operator loading with context")
            viewModel.loadIncidents(userId, source)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { handleRefresh() }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = when {
            userId != null -> "User Incidents"
            else -> "Incidents List"
        },
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ✅ Business and Site Filters with shared persistence
                if (currentUser?.role in listOf(
                    app.forku.domain.model.user.UserRole.ADMIN,
                    app.forku.domain.model.user.UserRole.SUPERADMIN,
                    app.forku.domain.model.user.UserRole.SYSTEM_OWNER
                )) {
                    BusinessSiteFilters(
                        mode = BusinessSiteFilterMode.VIEW_FILTER,
                        currentUserRole = currentUser?.role,
                        selectedBusinessId = filterBusinessId,
                        selectedSiteId = filterSiteId,
                        isAllSitesSelected = isAllSitesSelected,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onBusinessChanged = { businessId ->
                            sharedFiltersViewModel.setBusinessId(businessId)
                            // ✅ Data reload will be handled by LaunchedEffect observing filter changes
                        },
                        onSiteChanged = { siteId ->
                            Log.d("IncidentListScreen", "🎯 Site selection changed: $siteId")
                            if (siteId == "ALL_SITES") {
                                // "All Sites" selected - set site filter to null
                                Log.d("IncidentListScreen", "🔧 Processing ALL_SITES selection")
                                sharedFiltersViewModel.setSiteId(null)
                                Log.d("IncidentListScreen", "✅ All Sites selected - filtering with null siteId")
                            } else {
                                // Specific site selected
                                Log.d("IncidentListScreen", "🔧 Processing specific site selection: $siteId")
                                sharedFiltersViewModel.setSiteId(siteId)
                                Log.d("IncidentListScreen", "✅ Specific site selected: $siteId")
                            }
                            // ✅ Data reload will be handled by LaunchedEffect observing filter changes
                        },
                        showBusinessFilter = false,
                        isCollapsible = true,
                        initiallyExpanded = false,
                        title = "Filter Incidents",
                        adminSharedFiltersViewModel = sharedFiltersViewModel
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    when {
                        state.error != null -> {
                            val isAuthError = state.error?.contains("not authenticated", ignoreCase = true) == true
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isAuthError) "Authentication Required" else "Error",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = state.error ?: "Unknown error occurred",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = { handleRefresh() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAuthError) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(
                                        text = if (isAuthError) "Retry" else "Try Again"
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = state.incidents,
                                    key = { it.id }
                                ) { incident ->
                                    IncidentCard(
                                        type = incident.type,
                                        date = getRelativeTimeSpanFromMillis(incident.date),
                                        description = incident.description,
                                        status = try {
                                            IncidentStatus.valueOf(incident.status)
                                        } catch (e: IllegalArgumentException) {
                                            null
                                        },
                                        creatorName = incident.creatorName,
                                        onClick = {
                                            navController.navigate(
                                                Screen.IncidentDetail.route.replace(
                                                    "{incidentId}",
                                                    incident.id
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    PullRefreshIndicator(
                        refreshing = state.isLoading,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    )
} 