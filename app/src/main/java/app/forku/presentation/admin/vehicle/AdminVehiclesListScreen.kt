package app.forku.presentation.admin.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.user.UserRole
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import app.forku.presentation.vehicle.list.VehicleItem
import coil.ImageLoader
import app.forku.core.auth.TokenErrorHandler

@Composable
fun AdminVehiclesListScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    userRole: UserRole,
    imageLoader: ImageLoader,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: AdminVehiclesListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Admin - All Vehicles",
        showBackButton = true,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadVehicles() }) {
                        Text("Retry")
                    }
                }
            } else if (state.vehicles.isEmpty()) {
                Text("No vehicles found", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.vehicles) { vehicle ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(Screen.AdminVehicleProfile.createRoute(vehicle.id))
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                VehicleItem(
                                    vehicle = vehicle,
                                    userRole = userRole,
                                    imageLoader = imageLoader,
                                    onClick = { 
                                        navController.navigate(Screen.AdminVehicleProfile.createRoute(vehicle.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 