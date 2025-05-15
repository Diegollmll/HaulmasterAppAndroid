package app.forku.presentation.admin.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import app.forku.core.auth.TokenErrorHandler

@Composable
fun AdminVehicleProfileScreen(
    vehicleId: String,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    userRole: UserRole,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: AdminVehicleProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(vehicleId) {
        viewModel.loadVehicle(vehicleId)
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Admin - Vehicle Profile" + (state.vehicle?.codename?.let { " - $it" } ?: ""),
        showBackButton = true,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadVehicle(vehicleId) }) {
                        Text("Retry")
                    }
                }
            } else if (state.vehicle != null) {
                val vehicle = state.vehicle!!
                VehicleDetailsContent(
                    vehicle = vehicle,
                    onEditClick = {
                        navController.navigate(
                            Screen.EditVehicle.createRoute(
                                vehicleId = vehicle.id,
                                businessId = vehicle.businessId
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    "Vehicle not found.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun VehicleDetailsContent(
    vehicle: Vehicle,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header with Edit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = vehicle.codename,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Vehicle"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusItem(status = vehicle.status)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Basic Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Basic Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow("Model", vehicle.model)
                InfoRow("Serial Number", vehicle.serialNumber)
                InfoRow("Type", vehicle.type.Name)
                InfoRow("Category ID", vehicle.categoryId)
                InfoRow("Business", vehicle.businessId ?: "Unassigned")
                InfoRow("Energy Type", vehicle.energyType)
                InfoRow("Next Service", vehicle.nextService)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Description",
                    fontWeight = FontWeight.Bold
                )
                Text(text = vehicle.description)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Best Suited For",
                    fontWeight = FontWeight.Bold
                )
                Text(text = vehicle.bestSuitedFor)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Photo Model",
                    fontWeight = FontWeight.Bold
                )
                Text(text = vehicle.photoModel)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatusItem(status: VehicleStatus) {
    val statusColor = when(status) {
        VehicleStatus.AVAILABLE -> Color(0xFF4CAF50) // Green
        VehicleStatus.IN_USE -> Color(0xFF2196F3) // Blue
        VehicleStatus.OUT_OF_SERVICE -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }
    
    val statusText = when(status) {
        VehicleStatus.AVAILABLE -> "Available"
        VehicleStatus.IN_USE -> "In Use"
        VehicleStatus.OUT_OF_SERVICE -> "Out of Service"
        else -> status.name
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(16.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = statusColor
        ) { }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Normal
        )
    }
} 