package app.forku.presentation.vehicle.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.Vehicle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VehicleListScreen(
    navController: NavController,
    viewModel: VehicleListViewModel = hiltViewModel(),
    onVehicleClick: (Vehicle) -> Unit,
    networkManager: NetworkConnectivityManager,
    userRole: UserRole = UserRole.OPERATOR
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { viewModel.loadVehicles(true) }
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

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Vehicles",
        showBottomBar = false,
        onRefresh = { viewModel.loadVehicles(true) },
        showLoadingOnRefresh = false,
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadVehicles(true) }
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
                                            onClick = { onVehicleClick(vehicle) }
                                        )
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