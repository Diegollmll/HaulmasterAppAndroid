package app.forku.presentation.vehicle.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen

@Composable
fun VehicleListScreen(
    navController: NavController,
    viewModel: VehicleListViewModel = hiltViewModel(),
    onVehicleClick: (String) -> Unit = { vehicleId ->
        navController.navigate("vehicle_profile/${vehicleId}")
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BaseScreen(
        navController = navController,
        viewModel = viewModel,
        topBarTitle = "Vehicles",
        onRefresh = { viewModel.loadVehicles() }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> LoadingOverlay()
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadVehicles() }
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
                                onClick = { /* Handle vehicle click */ }
                            )
                        }
                    }
                }
            }
        }
    }
} 