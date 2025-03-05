package app.forku.presentation.dashboard

import android.text.format.DateUtils.formatDateTime
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.forku.R
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.ForkUBottomBar
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.navigation.Screen
import app.forku.presentation.session.SessionViewModel
import app.forku.presentation.user.login.LoginViewModel
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.session.SessionStatus
import app.forku.presentation.session.SessionState
import app.forku.presentation.vehicle.profile.components.VehicleStatusIndicator
import coil.compose.AsyncImage
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import app.forku.presentation.common.components.AppBottomSheet
import app.forku.presentation.common.viewmodel.BottomSheetViewModel
import app.forku.presentation.incident.components.IncidentTypeSelector
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import app.forku.presentation.common.components.BaseScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigate: (String) -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()
    val loginState by loginViewModel.state.collectAsStateWithLifecycle()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = dashboardState.isLoading,
        onRefresh = { dashboardViewModel.refreshWithLoading() }
    )

    BaseScreen(
        navController = navController,
        viewModel = dashboardViewModel,
        showTopBar = false,
        onRefresh = { dashboardViewModel.refresh() },
        showLoadingOnRefresh = true
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Only show active session card if there's an active vehicle
                dashboardState.activeVehicle?.let { vehicle ->
                    if (dashboardState.currentSession != null) {
                        ActiveSessionCard(
                            vehicle = vehicle,
                            status = dashboardState.vehicleStatus,
                            lastCheck = dashboardState.lastPreShiftCheck
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                // Navigation buttons grid
                DashboardNavigationButtons(
                    onNavigateToProfile = { onNavigate(Screen.Profile.route) },
                    onNavigateToIncidents = { onNavigate(Screen.IncidentsHistory.route) },
                    onNavigateToVehicles = { onNavigate(Screen.Vehicles.route) },
                    onNavigateToActivity = { onNavigate(Screen.OperatorsCICOHistory.route) },
                    onCheckOut = {
                        if (dashboardState.currentSession != null) {
                            sessionViewModel.endSession()
                        } else {
                            onNavigate(Screen.QRScanner.route)
                        }
                    },
                    showCheckOut = dashboardState.currentSession != null
                )
            }

            // Show error if any
            dashboardState.error?.let { error ->
                ErrorScreen(
                    message = error,
                    onRetry = {
                        dashboardViewModel.refresh()
                    }
                )
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
private fun ActiveSessionCard(
    vehicle: Vehicle,
    status: VehicleStatus,
    lastCheck: PreShiftCheck?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Active session",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = vehicle.photoUrl ?: R.drawable.ic_vehicle_placeholder,
                    contentDescription = "Vehicle image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(id = R.drawable.ic_vehicle_placeholder)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = vehicle.codename,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                    VehicleStatusIndicator(status = status)
                    Text(
                        text = "Pre-Shift Check: ${lastCheck?.status ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    lastCheck?.lastCheckDateTime?.let { dateTime ->
                        Text(
                            text = "Last Checked: ${formatDateTime(dateTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardNavigationButtons(
    onNavigateToProfile: () -> Unit,
    onNavigateToIncidents: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onCheckOut: () -> Unit,
    showCheckOut: Boolean
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
                icon = if (showCheckOut) Icons.Default.Close else Icons.Default.PlayArrow,
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
                icon = Icons.Default.Star,
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
                icon = Icons.Default.Menu,
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
                icon = Icons.Default.Search,
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
            width = if (isCenter) 2.dp else 1.dp,
            color = if (isCenter) Color.Green else Color.Gray
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
fun VehicleStatusMessage(
    sessionState: SessionState,
    vehicle: Vehicle?
) {
    android.util.Log.d(
        "appflow",
        "sessionState.session?.status =${sessionState.session?.status}"
    )
    val message = when {
        sessionState.session?.status == SessionStatus.ACTIVE ->
            vehicle?.let { "Active session" }
        else -> "No active session"
    }

    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
    }
}

@Composable
fun IncidentReportBottomSheet() {
    val viewModel: BottomSheetViewModel = hiltViewModel()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()

    if (showBottomSheet) {
        AppBottomSheet(
            onDismiss = { viewModel.hideBottomSheet() },
            content = {
                IncidentTypeSelector(
                    onTypeSelected = { type ->
                        // Handle type selection
                    },
                    onDismiss = { viewModel.hideBottomSheet() }
                )
            }
        )
    }
}

@Composable
private fun formatDateTime(dateTimeString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateTimeString)
        val localDateTime = java.time.LocalDateTime.ofInstant(
            instant,
            java.time.ZoneId.systemDefault()
        )
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")
        localDateTime.format(formatter)
    } catch (e: Exception) {
        dateTimeString // Return original string if parsing fails
    }
}
