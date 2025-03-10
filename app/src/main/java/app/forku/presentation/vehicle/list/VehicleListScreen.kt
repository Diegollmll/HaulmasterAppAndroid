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

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VehicleListScreen(
    navController: NavController,
    viewModel: VehicleListViewModel = hiltViewModel(),
    onVehicleClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { viewModel.loadVehicles(true) }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Vehicles",
        showBottomBar = true,
        onRefresh = { viewModel.loadVehicles(true) },
        showLoadingOnRefresh = false
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.vehicles) { vehicle ->
                            VehicleListItem(
                                vehicle = vehicle,
                                onClick = { onVehicleClick(vehicle.id) }
                            )
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