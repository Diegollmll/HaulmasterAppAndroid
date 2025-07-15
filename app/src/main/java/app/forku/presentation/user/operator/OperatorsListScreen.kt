package app.forku.presentation.user.operator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.auth.UserRoleManager
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.BusinessSiteFilterMode
import app.forku.presentation.dashboard.OperatorSessionInfo
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar
import app.forku.presentation.navigation.Screen
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel
import androidx.navigation.NavHostController
import app.forku.domain.model.user.UserRole

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OperatorsListScreen(
    navController: NavController,
    viewModel: OperatorsListViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
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
    
    // ✅ SIMPLIFIED: Estado para saber si los filtros ya han sido inicializados desde DataStore
    var filtersInitialized by remember { mutableStateOf(false) }
    // Bandera para saber si ya se leyó cada filtro al menos una vez
    var businessIdRead by remember { mutableStateOf(false) }
    var siteIdRead by remember { mutableStateOf(false) }
    var allSitesRead by remember { mutableStateOf(false) }

    // Observa los cambios y marca como leído cuando cada filtro se inicializa
    LaunchedEffect(filterBusinessId) { if (!businessIdRead && filterBusinessId != null) businessIdRead = true }
    LaunchedEffect(filterSiteId) { if (!siteIdRead) siteIdRead = true }
    LaunchedEffect(isAllSitesSelected) { if (!allSitesRead) allSitesRead = true }
    // Cuando los tres han sido leídos, marca filtersInitialized
    LaunchedEffect(businessIdRead, siteIdRead, allSitesRead) {
        if (businessIdRead && siteIdRead && allSitesRead) filtersInitialized = true
    }
    
    // ✅ CENTRALIZED: Single function to handle all operator loading scenarios
    fun loadOperatorsData() {
        val isAdmin = currentUser?.role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)
        
        if (isAdmin && filtersInitialized && filterBusinessId != null) {
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            Log.d("OperatorsListScreen", "[UI] loadOperatorsData: Admin loading with filters")
            Log.d("OperatorsListScreen", "[UI] Parameters: businessId=$filterBusinessId, siteId=$effectiveSiteId")
            viewModel.loadOperatorsWithFilters(filterBusinessId, effectiveSiteId)
        } else if (!isAdmin) {
            Log.d("OperatorsListScreen", "[UI] loadOperatorsData: Operator loading with context")
            viewModel.loadOperators(true)
        } else {
            Log.d("OperatorsListScreen", "[UI] loadOperatorsData skipped: isAdmin=$isAdmin, filtersInitialized=$filtersInitialized, filterBusinessId=$filterBusinessId")
        }
    }
    
    // ✅ SIMPLIFIED: Single LaunchedEffect for initial load and filter changes with debouncing
    LaunchedEffect(currentUser, filterBusinessId, filterSiteId, isAllSitesSelected, filtersInitialized) {
        Log.d("OperatorsListScreen", "=== OPERATORS LOAD TRIGGERED ===")
        Log.d("OperatorsListScreen", "[UI] LaunchedEffect triggered: filtersInitialized=$filtersInitialized, filterBusinessId=$filterBusinessId")
        
        // ✅ ADDED: Small delay to debounce rapid filter changes
        kotlinx.coroutines.delay(300)
        
        loadOperatorsData()
    }
    
    // ✅ CENTRALIZED: Single refresh function to avoid multiple triggers
    fun handleRefresh() {
        Log.d("OperatorsListScreen", "🔄 CENTRALIZED REFRESH: Loading operators")
        loadOperatorsData()
    }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { handleRefresh() }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Users",
        showBottomBar = false,
        showLoadingOnRefresh = false,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ✅ FILTERS: Separate from user context - for data visualization only
                if (currentUser?.role in listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER)) {
                    item {
                        BusinessSiteFilters(
                            mode = BusinessSiteFilterMode.VIEW_FILTER,
                            currentUserRole = currentUser?.role,
                            selectedBusinessId = filterBusinessId,
                            selectedSiteId = if (isAllSitesSelected) null else filterSiteId,
                            isAllSitesSelected = isAllSitesSelected,
                            onBusinessChanged = { businessId ->
                                sharedFiltersViewModel.setBusinessId(businessId)
                            },
                            onSiteChanged = { siteId ->
                                Log.d("OperatorsListScreen", "🎯 Site selection changed: $siteId")
                                if (siteId == "ALL_SITES") {
                                    // "All Sites" selected - set site filter to null
                                    Log.d("OperatorsListScreen", "🔧 Processing ALL_SITES selection")
                                    sharedFiltersViewModel.setSiteId(null)
                                    Log.d("OperatorsListScreen", "✅ All Sites selected - filtering with null siteId")
                                } else {
                                    // Specific site selected
                                    Log.d("OperatorsListScreen", "🔧 Processing specific site selection: $siteId")
                                    sharedFiltersViewModel.setSiteId(siteId)
                                    Log.d("OperatorsListScreen", "✅ Specific site selected: $siteId")
                                }
                                // ✅ Data reload will be handled by LaunchedEffect observing filter changes
                            },
                            showBusinessFilter = false,
                            isCollapsible = true,
                            initiallyExpanded = false,
                            title = "Filter Users",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            adminSharedFiltersViewModel = sharedFiltersViewModel
                        )
                    }
                }
                
                // ✅ UPDATED: Filter context header (not business context)
                item {
                    FilterContextHeader(
                        businessId = filterBusinessId,
                        siteId = if (isAllSitesSelected) null else filterSiteId,
                        isAllSitesSelected = isAllSitesSelected,
                        totalUsers = state.operators.size,
                        activeUsers = state.operators.count { it.isActive }
                    )
                }
                
                if (state.isLoading && !state.isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.operators.isEmpty() && !state.isLoading) {
                    item {
                        NoUsersMessage(
                            hasFilters = filterBusinessId != null,
                            onRefresh = { handleRefresh() }
                        )
                    }
                } else {
                    items(state.operators) { operator ->
                        OperatorItem(
                            operator = operator,
                            onClick = { 
                                navController.navigate(Screen.Profile.createRoute(operator.userId))
                            }
                        )
                    }
                }
                
                state.error?.let { error ->
                    item {
                        ErrorMessage(
                            message = error,
                            onRetry = { handleRefresh() }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isLoading && state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun FilterContextHeader(
    businessId: String?,
    siteId: String?,
    isAllSitesSelected: Boolean,
    totalUsers: Int,
    activeUsers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter Context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!businessId.isNullOrBlank()) {
                Text(
                    text = "Business ID: $businessId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isAllSitesSelected) {
                    Text(
                        text = "Site: All Sites",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!siteId.isNullOrBlank()) {
                    Text(
                        text = "Site ID: $siteId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Users: $totalUsers",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Active: $activeUsers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activeUsers > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No filter context available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NoUsersMessage(
    hasFilters: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasFilters) {
                    "No users found with current filters"
                } else {
                    "No filter context available"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (hasFilters) {
                    "Try adjusting your filters or check if there are users assigned to this business/site."
                } else {
                    "Unable to determine current filter context."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRefresh
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun OperatorItem(
    operator: OperatorSessionInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarData = getUserAvatarData(
                    operator.name.split(" ").firstOrNull(),
                    operator.name.split(" ").drop(1).firstOrNull(),
                    operator.image
                ),
                size = 48.dp,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = operator.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "@${operator.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = UserRoleManager.toDisplayString(operator.role),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (operator.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
            )
        }
    }
} 