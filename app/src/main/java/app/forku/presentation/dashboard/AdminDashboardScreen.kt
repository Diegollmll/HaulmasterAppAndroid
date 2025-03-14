package app.forku.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController? = null,
    onNavigate: (String) -> Unit = {},
    viewModel: AdminDashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    networkManager: NetworkConnectivityManager
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val dashboardState by viewModel.state.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = dashboardState.isLoading,
        onRefresh = { viewModel.loadDashboardData() }
    )
    
    BaseScreen(
        navController = navController ?: return,
        showBottomBar = true,
        showTopBar = false,
        showBackButton = false,
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { HeaderSection(userFirstName = currentUser?.firstName ?: "", navController) }
                
                item { OperationStatusSection(dashboardState) }
                
                item { VehicleSessionSection(dashboardState, navController) }
                
                item { OperatorsSessionSection(dashboardState, navController) }
                
                // Add some padding at the bottom
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            PullRefreshIndicator(
                refreshing = dashboardState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun HeaderSection(
    userFirstName: String,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hi, $userFirstName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "How are you today?",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
        IconButton(onClick = { 
            navController.navigate(Screen.Notifications.route)
        }) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun OperationStatusSection(state: AdminDashboardState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operation Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    icon = Icons.Default.DirectionsCar,
                    count = state.operatingVehiclesCount.toString(),
                    label = "Operating",
                    iconTint = Color(0xFF4CAF50)
                )
                StatusItem(
                    icon = Icons.Default.Warning,
                    count = state.totalIncidentsCount.toString(),
                    label = "Incidents",
                    iconTint = Color(0xFFFFA726)
                )
                StatusItem(
                    icon = Icons.Default.Security,
                    count = state.safetyAlertsCount.toString(),
                    label = "Safety Alerts",
                    iconTint = Color(0xFF2196F3)
                )
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusItem(
    icon: ImageVector,
    count: String,
    total: String? = null,
    label: String,
    iconTint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        if (total != null) {
            Text(
                text = total,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = count,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun VehicleSessionSection(
    state: AdminDashboardState,
    navController: NavController
) {
    Column {
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
                onClick = { navController.navigate(Screen.VehicleSessionList.route) }
            ) {
                Text("View all")
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (state.activeVehicleSessions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active vehicle sessions",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    state.activeVehicleSessions.forEachIndexed { index, session ->
                        VehicleSessionItem(
                            vehicleId = session.vehicleId,
                            vehicleType = session.vehicleType,
                            progress = session.progress,
                            operatorName = session.operatorName,
                            operatorImage = session.operatorImage ?: "",
                            vehicleImage = session.vehicleImage,
                            codename = session.codename
                        )
                        
                        if (index < state.activeVehicleSessions.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
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

@Composable
private fun VehicleSessionItem(
    vehicleId: String,
    vehicleType: String,
    progress: Float,
    operatorName: String,
    operatorImage: String,
    vehicleImage: String?,
    codename: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = vehicleImage,
                contentDescription = "Vehicle image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = codename,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = vehicleType,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            progress > 0.7f -> Color(0xFF4CAF50)
                            progress > 0.3f -> Color(0xFFFFA726)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(100.dp)
            ) {
                AsyncImage(
                    model = operatorImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = "Operator",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = operatorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun OperatorsSessionSection(state: AdminDashboardState, navController: NavController) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Operators in-Session",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { navController.navigate(Screen.OperatorSessionList.route) }) {
                Text("View all")
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        if (state.activeOperators.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No operators currently in session",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.activeOperators.forEach { operator ->
                    OperatorItem(
                        name = operator.name,
                        image = operator.image ?: "",
                        isActive = operator.isActive
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

@Composable
private fun OperatorItem(
    name: String,
    image: String,
    isActive: Boolean
) {
    Card(
        modifier = Modifier.width(111.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isActive) "Active" else "Inactive",
                fontSize = 12.sp,
                color = if (isActive) Color(0xFF4CAF50) else Color.Gray
            )
        }
    }
} 