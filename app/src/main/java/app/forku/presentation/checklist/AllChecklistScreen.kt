package app.forku.presentation.checklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.checklist.CheckStatus
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import app.forku.core.auth.TokenErrorHandler
import app.forku.domain.model.checklist.getPreShiftStatusText
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.BusinessSiteFilterMode
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel
import androidx.navigation.NavHostController
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun AllChecklistScreen(
    navController: NavController,
    viewModel: AllChecklistViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val navHostController = navController as? NavHostController
    val owner = LocalViewModelStoreOwner.current
    val sharedFiltersViewModel: AdminSharedFiltersViewModel = hiltViewModel(viewModelStoreOwner = owner!!)

    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val filterBusinessId by sharedFiltersViewModel.filterBusinessId.collectAsStateWithLifecycle()
    val filterSiteId by sharedFiltersViewModel.filterSiteId.collectAsStateWithLifecycle()
    val isAllSitesSelected by sharedFiltersViewModel.isAllSitesSelected.collectAsStateWithLifecycle()
    // ✅ REMOVED: availableSites - BusinessSiteFilters handles sites internally

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

    // ✅ REMOVED: LaunchedEffect for loading sites - BusinessSiteFilters handles this internally
    
    // ✅ CENTRALIZED: Single function to handle all checklist loading scenarios
    fun loadChecklistData(page: Int = 1, append: Boolean = false) {
        if (filtersInitialized && filterBusinessId != null) {
            val effectiveSiteId = if (isAllSitesSelected) null else filterSiteId
            Log.d("AllChecklistScreen", "[UI] loadChecklistData called: page=$page, append=$append")
            Log.d("AllChecklistScreen", "[UI] Parameters: businessId=$filterBusinessId, siteId=$effectiveSiteId")
            viewModel.loadChecksWithFilters(filterBusinessId, effectiveSiteId, page, append)
        } else {
            Log.d("AllChecklistScreen", "[UI] loadChecklistData skipped: filtersInitialized=$filtersInitialized, filterBusinessId=$filterBusinessId")
        }
    }
    
    // Calculate if we should load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= listState.layoutInfo.totalItemsCount - 2
        }
    }
    
    // ✅ SIMPLIFIED: Single LaunchedEffect for initial load and filter changes
    LaunchedEffect(filterBusinessId, filterSiteId, isAllSitesSelected, filtersInitialized) {
        Log.d("AllChecklistScreen", "=== CHECKLIST LOAD TRIGGERED ===")
        Log.d("AllChecklistScreen", "[UI] LaunchedEffect triggered: filtersInitialized=$filtersInitialized, filterBusinessId=$filterBusinessId")
        loadChecklistData(page = 1, append = false)
    }
    
    // ✅ CENTRALIZED: Pagination loading
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.isLoading && !state.isLoadingMore && state.hasMoreItems) {
            Log.d("AllChecklistScreen", "[UI] Pagination triggered: shouldLoadMore=$shouldLoadMore")
            loadChecklistData(page = state.currentPage + 1, append = true)
        }
    }

    // ✅ CENTRALIZED: Single refresh function
    fun handleRefresh() {
        Log.d("AllChecklistScreen", "🔄 CENTRALIZED REFRESH: Loading with filters")
        loadChecklistData(page = 1, append = false)
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        topBarTitle = "All Checks",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ✅ Business and Site Filters with shared persistence
            BusinessSiteFilters(
                mode = BusinessSiteFilterMode.VIEW_FILTER,
                currentUserRole = currentUser?.role, // Pass the actual user role
                selectedBusinessId = filterBusinessId,
                selectedSiteId = filterSiteId,
                isAllSitesSelected = isAllSitesSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onBusinessChanged = { businessId ->
                    Log.d("AllChecklistScreen", "[UI] onBusinessChanged: $businessId")
                    sharedFiltersViewModel.setBusinessId(businessId)
                    // ✅ Data reload will be handled by LaunchedEffect observing filter changes
                },
                onSiteChanged = { siteId ->
                    Log.d("AllChecklistScreen", "[UI] onSiteChanged: $siteId")
                    if (siteId == "ALL_SITES") {
                        Log.d("AllChecklistScreen", "[UI] ALL_SITES selected")
                        sharedFiltersViewModel.setSiteId(null)
                    } else {
                        sharedFiltersViewModel.setSiteId(siteId)
                    }
                },
                showBusinessFilter = false,
                isCollapsible = true,
                initiallyExpanded = false,
                title = "Filter Checks",
                adminSharedFiltersViewModel = sharedFiltersViewModel
            )
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (state.checks.isEmpty()) {
                    Text(
                        text = "No checks found",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.checks) { check ->
                                CheckCard(
                                    check = check,
                                    onClick = {
                                        navController.navigate(Screen.CheckDetail.createRoute(check.id))
                                    }
                                )
                            }
                        }
                        
                        // Show loading indicator at bottom while loading more
                        if (state.isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
                
                state.error?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckCard(
    check: PreShiftCheckState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle: ${check.vehicleCodename}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Operator: ${check.operatorName}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                StatusChip(status = getPreShiftStatusText(check.status?.toIntOrNull()))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = check.lastCheckDateTime?.let { 
                    "Last updated: ${getRelativeTimeSpanString(it)}"
                } ?: "No update time available",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
public fun StatusChip(status: String) {
    val statusEnum = try {
        CheckStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (statusEnum) {
            CheckStatus.COMPLETED_PASS -> Color(0xFF4CAF50)
            CheckStatus.COMPLETED_FAIL -> Color.Red
            CheckStatus.IN_PROGRESS -> Color(0xFFFFA726)
            else -> Color.Gray
        }.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = when (statusEnum) {
                    CheckStatus.COMPLETED_PASS -> Color(0xFF4CAF50)
                    CheckStatus.COMPLETED_FAIL -> Color.Red
                    CheckStatus.IN_PROGRESS -> Color(0xFFFFA726)
                    else -> Color.Gray
                },
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = status,
                color = when (statusEnum) {
                    CheckStatus.COMPLETED_PASS -> Color(0xFF4CAF50)
                    CheckStatus.COMPLETED_FAIL -> Color.Red
                    CheckStatus.IN_PROGRESS -> Color(0xFFFFA726)
                    else -> Color.Gray
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatDateTime(dateTime: String): String {
    return try {
        val instant = Instant.parse(dateTime)
        val formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateTime
    }
}