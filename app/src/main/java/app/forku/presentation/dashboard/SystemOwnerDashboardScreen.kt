package app.forku.presentation.dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import app.forku.domain.model.user.UserRole
import app.forku.presentation.common.components.DashboardHeader
import app.forku.presentation.common.components.FeedbackBanner

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SystemOwnerDashboardScreen(
    navController: NavController? = null,
    onNavigate: (String) -> Unit = {},
    viewModel: SystemOwnerDashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    networkManager: NetworkConnectivityManager
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
        showBottomBar = true,
        showTopBar = false,
        showBackButton = false,
        dashboardState = regularDashboardState,
        networkManager = networkManager
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
                    
                    item { SystemHealthSection(dashboardState, navController) }
                    
                    item { BusinessOverviewSection(dashboardState, navController) }
                    
                    item { UserManagementSection(dashboardState, navController) }
                    
                    item { SystemSettingsSection(dashboardState, navController) }
                    
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
    state: SystemOwnerDashboardState,
    navController: NavController
) {
    Log.d("SystemOwnerDashboard", "Rendering SystemOverviewSection with state: " +
        "users=${state.totalUsersCount}, " +
        "businesses=${state.totalBusinessCount}, " +
        "vehicles=${state.totalVehiclesCount}")
        
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
                SystemStatusItem(
                    icon = Icons.Default.People,
                    count = state.totalUsersCount.toString(),
                    label = "Users",
                    iconTint = Color(0xFF2196F3),
                    onClick = { navController.navigate(Screen.UserManagement.route) }
                )
                SystemStatusItem(
                    icon = Icons.Default.Business,
                    count = state.totalBusinessCount.toString(),
                    label = "Businesses",
                    iconTint = Color(0xFF673AB7),
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                SystemStatusItem(
                    icon = Icons.Default.DirectionsCar,
                    count = state.totalVehiclesCount.toString(),
                    label = "Vehicles",
                    iconTint = Color(0xFF4CAF50),
                    onClick = { navController.navigate(Screen.VehiclesList.route) }
                )
            }
        }
    }
}

@Composable
private fun SystemStatusItem(
    icon: ImageVector,
    count: String,
    label: String,
    iconTint: Color,
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
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SystemHealthSection(
    state: SystemOwnerDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "System Health",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthStatusItem(
                    icon = Icons.Default.Speed,
                    count = "${state.apiLatency}ms",
                    label = "API Latency",
                    iconTint = Color(0xFF2196F3),
                    onClick = { navController.navigate(Screen.SystemSettings.route) }
                )
                HealthStatusItem(
                    icon = Icons.Default.Storage,
                    count = state.databaseSize,
                    label = "Database",
                    iconTint = Color(0xFF673AB7),
                    onClick = { navController.navigate(Screen.SystemBackup.route) }
                )
                HealthStatusItem(
                    icon = Icons.Default.Person,
                    count = state.activeConnections.toString(),
                    label = "Connections",
                    iconTint = Color(0xFF4CAF50),
                    onClick = { navController.navigate(Screen.SystemSettings.route) }
                )
            }
        }
    }
}

@Composable
private fun HealthStatusItem(
    icon: ImageVector,
    count: String,
    label: String,
    iconTint: Color,
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
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BusinessOverviewSection(
    state: SystemOwnerDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Business Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BusinessStatusItem(
                    icon = Icons.Default.CheckCircle,
                    count = state.activeBusinesses.toString(),
                    label = "Active",
                    iconTint = Color(0xFF4CAF50),
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                BusinessStatusItem(
                    icon = Icons.Default.Pending,
                    count = state.pendingBusinesses.toString(),
                    label = "Pending",
                    iconTint = Color(0xFFFFA726),
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
                BusinessStatusItem(
                    icon = Icons.Default.Block,
                    count = state.suspendedBusinesses.toString(),
                    label = "Suspended",
                    iconTint = Color(0xFFF44336),
                    onClick = { navController.navigate(Screen.BusinessManagement.route) }
                )
            }
        }
    }
}

@Composable
private fun BusinessStatusItem(
    icon: ImageVector,
    count: String,
    label: String,
    iconTint: Color,
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
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun UserManagementSection(
    state: SystemOwnerDashboardState,
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
                    icon = Icons.Default.Group,
                    text = "Manage Roles",
                    onClick = { navController.navigate(Screen.RoleManagement.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Security,
                    text = "Permissions",
                    onClick = { navController.navigate(Screen.PermissionsManagement.route) }
                )
            }
        }
    }
}

@Composable
private fun SystemSettingsSection(
    state: SystemOwnerDashboardState,
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
                    onClick = { navController.navigate(Screen.AuditLog.route) }
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