package app.forku.presentation.dashboard

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
import app.forku.domain.model.user.UserRole
import app.forku.presentation.vehicle.list.VehicleItem
import app.forku.presentation.common.components.DashboardHeader
import app.forku.presentation.common.components.FeedbackBanner
import coil.ImageLoader
import app.forku.core.auth.TokenErrorHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    imageLoader: ImageLoader,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // Enhanced logging for debugging operating vehicles count
    LaunchedEffect(state.operatingVehiclesCount, state.activeVehicleSessions.size) {
        Log.d("AdminDashboardScreen", "=== UI STATE UPDATE ===")
        Log.d("AdminDashboardScreen", "operatingVehiclesCount (from API): ${state.operatingVehiclesCount}")
        Log.d("AdminDashboardScreen", "activeVehicleSessions.size (from UI): ${state.activeVehicleSessions.size}")
        Log.d("AdminDashboardScreen", "userIncidentsCount: ${state.userIncidentsCount}")
        Log.d("AdminDashboardScreen", "totalIncidentsCount: ${state.totalIncidentsCount}")
        Log.d("AdminDashboardScreen", "safetyAlertsCount: ${state.safetyAlertsCount}")
        Log.d("AdminDashboardScreen", "isLoading: ${state.isLoading}")
        Log.d("AdminDashboardScreen", "error: ${state.error}")
        Log.d("AdminDashboardScreen", "========================")
    }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val checklistAnswers by viewModel.checklistAnswers.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.authEvent.collect { event ->
            when (event) {
                is AuthEvent.NavigateToLogin -> {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }

    BaseScreen(
        navController = navController,
        showTopBar = false,
        showBottomBar = true,
        dashboardState = DashboardState(user = currentUser),
        topBarTitle = "Admin Dashboard",
        onRefresh = { viewModel.refreshWithLoading() },
        showLoadingOnRefresh = true,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(rememberPullRefreshState(
                    refreshing = state.isLoading,
                    onRefresh = { viewModel.loadDashboardData() }
                )),
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
                            onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                            onSettingsClick = { navController.navigate(Screen.SystemSettings.route) }
                        )
                    }
                    
                    item { OperationStatusSection(state, navController) }
                    
                    item { VehicleSessionSection(state, navController, imageLoader, checklistAnswers) }
                    
                    item { OperatorsInSessionSection(state, navController) }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        FeedbackBanner(
                            onFeedbackSubmitted = { rating, feedback, canContactMe ->
                                viewModel.submitFeedback(rating, feedback, canContactMe)
                            }
                        )
                        
                        // Show success message when feedback is submitted
                        if (state.feedbackSubmitted) {
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
                refreshing = state.isLoading,
                state = rememberPullRefreshState(
                    refreshing = state.isLoading,
                    onRefresh = { viewModel.loadDashboardData() }
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun OperationStatusSection(
    state: AdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operation Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    icon = Icons.Default.DirectionsCar,
                    count = state.operatingVehiclesCount.toString(),
                    label = "Operating",
                    iconTint = Color(0xFF4CAF50),
                    onClick = { navController.navigate(Screen.VehiclesList.route) }
                )
                Log.d("AdminDashboardScreen", "OperationStatusSection - totalIncidentsCount: ${state.totalIncidentsCount}")
                StatusItem(
                    icon = Icons.Default.Warning,
                    count = state.totalIncidentsCount.toString(),
                    label = "Incidents",
                    iconTint = Color(0xFFFFA726),
                    onClick = { navController.navigate(Screen.IncidentList.createRoute()) }
                )
                StatusItem(
                    icon = Icons.Default.Security,
                    count = state.safetyAlertsCount.toString(),
                    label = "Safety Alerts",
                    iconTint = Color(0xFF2196F3),
                    onClick = { navController.navigate(Screen.SafetyAlerts.route) }
                )
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun StatusItem(
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
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun VehicleSessionSection(
    state: AdminDashboardState,
    navController: NavController,
    imageLoader: ImageLoader,
    checklistAnswers: Map<String, app.forku.domain.model.checklist.ChecklistAnswer>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
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
                    onClick = { navController.navigate(Screen.VehiclesList.route) }
                ) {
                    Text(
                        "View all",
                        fontSize = 10.sp
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.activeVehicleSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active vehicle sessions",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.activeVehicleSessions.forEach { vehicleSessionInfo ->
                        VehicleItem(
                            vehicle = vehicleSessionInfo.vehicle,
                            userRole = UserRole.OPERATOR,
                            sessionInfo = vehicleSessionInfo,
                            showStatus = false,
                            lastPreShiftCheck = state.lastPreShiftChecks[vehicleSessionInfo.vehicle.id],
                            imageLoader = imageLoader,
                            checklistAnswer = checklistAnswers[vehicleSessionInfo.session.checkId],
                            onClick = {
                                navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleSessionInfo.vehicle.id))
                            }
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
}

@Composable
private fun OperatorsInSessionSection(
    state: AdminDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operators In-Session",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate(Screen.OperatorsList.route) }
                ) {
                    Text(
                        "View all",
                        fontSize = 10.sp
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.activeOperators.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No operators in session",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.activeOperators.forEach { operator ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            UserAvatar(
                                avatarData = getUserAvatarData(
                                    operator.name.split(" ").firstOrNull(),
                                    operator.name.split(" ").drop(1).firstOrNull(),
                                    operator.image
                                ),
                                size = 24.dp,
                                fontSize = 12.sp
                            )
                            Text(
                                text = operator.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
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
}
