package app.forku.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.presentation.navigation.Screen
import app.forku.domain.model.vehicle.VehicleStatus
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.Menu
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import app.forku.domain.model.vehicle.toColor
import app.forku.presentation.dashboard.components.SessionCard


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val dashboardState by viewModel.state.collectAsState()
    
    // Add loading state observation
    var isCheckoutLoading by remember { mutableStateOf(false) }

    // Handle loading state during checkout
    LaunchedEffect(dashboardState.currentSession) {
        if (dashboardState.currentSession == null && isCheckoutLoading) {
            isCheckoutLoading = false
        }
    }

    // Handle errors
    LaunchedEffect(dashboardState.error) {
        dashboardState.error?.let {
            isCheckoutLoading = false
        }
    }

    // Effect to refresh dashboard when screen becomes active
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = dashboardState.isLoading,
        onRefresh = { viewModel.refreshWithLoading() }
    )

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        currentVehicleId = dashboardState.currentSession?.vehicleId,
        currentCheckId = dashboardState.lastPreShiftCheck?.id,
        dashboardState = dashboardState
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SessionCard(
                    vehicle = dashboardState.displayVehicle,
                    lastCheck = dashboardState.lastPreShiftCheck,
                    user = dashboardState.user,
                    currentSession = dashboardState.currentSession,
                    onCheckClick = { checkId ->
                        dashboardState.displayVehicle?.id?.let { vehicleId ->
                            onNavigate("checklist/$vehicleId?checkId=$checkId")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                DashboardNavigationButtons(
                    onNavigateToProfile = { onNavigate(Screen.Profile.route) },
                    onNavigateToIncidents = { onNavigate(Screen.IncidentsHistory.route) },
                    onNavigateToVehicles = { onNavigate(Screen.Vehicles.route) },
                    onNavigateToActivity = { onNavigate(Screen.OperatorsCICOHistory.route) },
                    onNavigateToManual = { onNavigate(Screen.PerformanceReport.route) },
                    onCheckOut = {
                        if (dashboardState.currentSession != null) {
                            isCheckoutLoading = true
                            viewModel.endCurrentSession()
                        } else {
                            onNavigate(Screen.QRScanner.route)
                        }
                    },
                    showCheckOut = dashboardState.currentSession != null,
                    isCheckoutLoading = isCheckoutLoading
                )
            }

            // Show loading indicator
            if (isCheckoutLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Show error if any
            dashboardState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(error)
                }
            }

            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = dashboardState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DashboardNavigationButtons(
    onNavigateToProfile: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToManual: () -> Unit,
    onCheckOut: () -> Unit,
    showCheckOut: Boolean,
    isCheckoutLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(24.dp)
    ) {
        // Center Check In/Out Button
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp)
        ) {
            NavigationButton(
                icon = if (showCheckOut) Icons.Default.Close else Icons.Default.QrCodeScanner,
                text = if (showCheckOut) "Check Out" else "Check In",
                onClick = onCheckOut,
                isCenter = true
            )
        }

        // Outer buttons with adjusted padding
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(bottom = 16.dp)
        ) {
            NavigationButton(
                icon = Icons.Default.Person,
                text = "Profile",
                onClick = onNavigateToProfile
            )
        }

        // Right - Goals
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(start = 8.dp)
        ) {
            NavigationButton(
                icon = Icons.Default.ClearAll,
                text = "Vehicles",
                onClick = onNavigateToVehicles
            )
        }

        // Bottom - History
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 8.dp)
        ) {
            NavigationButton(
                icon = Icons.Default.History,
                text = "CICO",
                onClick = onNavigateToActivity
            )
        }

        // Left - Awareness
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 8.dp)
        ) {
            NavigationButton(
                icon = Icons.Default.Report,
                text = "Incidents",
                onClick = onNavigateToIncidents
            )
        }

    }
}

@Composable
private fun NavigationButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isCenter: Boolean = false,
    modifier: Modifier = Modifier
) {
    val buttonSize = if (isCenter) 120.dp else 90.dp
    val iconSize = if (isCenter) 32.dp else 24.dp
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        shape = CircleShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(
            width = if (isCenter) 3.dp else 0.dp,
            color = if (isCenter) VehicleStatus.AVAILABLE.toColor().copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(0.dp).fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                maxLines = 1
            )
        }
    }
}


