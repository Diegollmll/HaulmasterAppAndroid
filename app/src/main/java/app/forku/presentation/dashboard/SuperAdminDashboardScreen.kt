package app.forku.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.presentation.navigation.Screen
import app.forku.presentation.common.components.DashboardHeader
import app.forku.presentation.common.components.FeedbackBanner
import androidx.hilt.navigation.compose.hiltViewModel

import app.forku.presentation.common.components.ErrorBanner
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.forku.core.auth.TokenErrorHandler
import app.forku.domain.model.business.BusinessStatus


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SuperAdminDashboardScreen(
    navController: NavController? = null,
    onNavigate: (String) -> Unit = {},
    viewModel: SuperAdminDashboardViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val dashboardState by viewModel.state.collectAsState()

    val regularDashboardState = DashboardState(
        isLoading = dashboardState.isLoading,
        error = dashboardState.error,
        user = currentUser,
        isAuthenticated = true
    )

    val pullRefreshState = rememberPullRefreshState(
        refreshing = dashboardState.isLoading,
        onRefresh = { viewModel.loadDashboardData() }
    )
    
    BaseScreen(
        navController = navController ?: return,
        showBottomBar = false,
        showTopBar = false,
        showBackButton = false,
        dashboardState = regularDashboardState,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
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
                            onNotificationClick = { navController?.navigate(Screen.Notifications.route) },
                            onProfileClick = { navController?.navigate(Screen.Profile.route) }
                        )
                    }
                    
                    item { SystemOverviewSection(dashboardState, navController) }
                    
                    item { UserAndSettingsSection(dashboardState, navController) }
                    
//                    item { BusinessManagementSection(dashboardState, navController) }
//
                    item { VehicleManagementSection(dashboardState, navController) }
                    
                    //item { SystemSettingsSection(dashboardState, navController) }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        FeedbackBanner(
                            onFeedbackSubmitted = { rating, feedback ->
                                viewModel.submitFeedback(rating, feedback)
                            }
                        )
                        
                        if (dashboardState.feedbackSubmitted) {
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
                refreshing = dashboardState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun SystemOverviewSection(
    state: SuperAdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    icon = Icons.Default.Business,
                    count = state.totalBusinessCount.toString(),
                    label = "Businesses",
                    iconTint = Color(0xFF673AB7),
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                StatusItem(
                    icon = Icons.Default.People,
                    count = state.totalUsersCount.toString(),
                    label = "Users",
                    iconTint = Color(0xFF2196F3),
                    onClick = { navController.navigate(Screen.UserManagement.route) }
                )
                StatusItem(
                    icon = Icons.Default.DirectionsCar,
                    count = state.totalVehiclesCount.toString(),
                    label = "Vehicles",
                    iconTint = Color(0xFF4CAF50),
                    onClick = { navController.navigate(Screen.AdminVehiclesList.route) }
                )
            }
        }
    }
}

@Composable
private fun UserAndSettingsSection(
    state: SuperAdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminActionButton(
                    icon = Icons.Default.PersonAdd,
                    text = "Add User",
                    onClick = { navController.navigate(Screen.AddUser.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Business,
                    text = "Add Business",
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Groups,
                    text = "Assign Users",
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
            }
        }
    }
}

@Composable
private fun BusinessManagementSection(
    state: SuperAdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Business Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminActionButton(
                    icon = Icons.Default.Business,
                    text = "Add Business",
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Groups,
                    text = "Assign Users",
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Add,
                    text = "Add Vehicle",
                    onClick = { navController.navigate(Screen.AddVehicle.route) }
                )
            }
        }
    }
}

@Composable
private fun VehicleManagementSection(
    state: SuperAdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vehicle Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminActionButton(
                    icon = Icons.Default.Add,
                    text = "Add Vehicle",
                    onClick = { navController.navigate(Screen.AddVehicle.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Build,
                    text = "Maintenance",
                    onClick = { navController.navigate(Screen.MaintenanceSchedule.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Assessment,
                    text = "Reports",
                    onClick = { navController.navigate(Screen.VehicleReports.route) }
                )
            }
        }
    }
}

@Composable
private fun SystemSettingsSection(
    state: SuperAdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminActionButton(
                    icon = Icons.Default.Settings,
                    text = "Settings",
                    onClick = { navController.navigate(Screen.SystemSettings.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Backup,
                    text = "Backup",
                    onClick = { navController.navigate(Screen.SystemBackup.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.History,
                    text = "Audit Log",
                    onClick = { navController.navigate(Screen.TimeZones.route) }
                )
            }
        }
    }
}

@Composable
private fun AdminActionButton(
    icon: ImageVector,
    text: String,
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BusinessStatusSection(
    totalBusinesses: Int,
    businessesByStatus: Map<BusinessStatus, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Business Statistics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Total Businesses: $totalBusinesses",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            businessesByStatus.forEach { (status, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = status.name)
                    Text(text = count.toString())
                }
            }
        }
    }
}

@Composable
private fun RecentBusinessesSection(
    businesses: List<Business>,
    onBusinessClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Businesses",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            businesses.forEach { business ->
                BusinessItem(
                    business = business,
                    onClick = { onBusinessClick(business.id) }
                )
            }
        }
    }
}

@Composable
private fun BusinessItem(
    business: Business,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = business.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Status: ${business.status.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "View details"
        )
    }
    Divider()
} 