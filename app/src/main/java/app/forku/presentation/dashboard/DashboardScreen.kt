package app.forku.presentation.dashboard

import app.forku.presentation.session.SessionState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.forku.domain.model.vehicle.Vehicle
import app.forku.presentation.user.login.LoginViewModel
import app.forku.presentation.common.components.ForkUBottomBar
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.navigation.Screen
import app.forku.presentation.vehicle.profile.components.VehicleProfileSummary
import app.forku.presentation.session.SessionViewModel
import app.forku.domain.model.session.SessionStatus
import app.forku.presentation.common.components.AppBottomSheet
import app.forku.presentation.common.viewmodel.BottomSheetViewModel
import app.forku.presentation.incident.components.IncidentTypeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController = rememberNavController(),
    onNavigate: (String) -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val sessionState by sessionViewModel.state.collectAsState()

    // Add debug log
    LaunchedEffect(dashboardState) {
        android.util.Log.d("Dashboard", "State updated: vehicle=${dashboardState.activeVehicle}, user=${dashboardState.user}")
    }

    Scaffold(
        modifier = Modifier.background(Color.Black),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    VehicleStatusMessage(
                        sessionState = sessionState,
                        vehicle = dashboardState.activeVehicle
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            loginViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFFFA726)
                        )
                    }
                }
            )
        },
        bottomBar = { ForkUBottomBar(navController = navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show vehicle summary if there's an active vehicle
                dashboardState.activeVehicle?.let { vehicle ->
                    VehicleProfileSummary(
                        vehicle = vehicle,
                        status = dashboardState.vehicleStatus,
                        modifier = Modifier.padding(bottom = 24.dp),
                        containerColor = Color.White,
                        showVehicleDetails = false

                    )
                }
                
                // Circular menu
                CircularMenu(
                    onNavigate = onNavigate,
                    sessionViewModel = sessionViewModel,
                    dashboardState = dashboardState
                )
            }

            // Show loading overlay
            if (dashboardState.isLoading) {
                LoadingOverlay()
            }

            // Show error if any
            dashboardState.error?.let { error ->
                ErrorScreen(
                    message = error,
                    onRetry = { dashboardViewModel.loadDashboard() }
                )
            }
        }
    }
}

@Composable
private fun CircularMenu(
    onNavigate: (String) -> Unit,
    sessionViewModel: SessionViewModel,
    dashboardState: DashboardState
) {
    val sessionState by sessionViewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Profile button
            CircularMenuItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = { onNavigate(Screen.Profile.route) }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Incidents button
                CircularMenuItem(
                    icon = Icons.Default.Search,
                    label = "Incidents",
                    onClick = { onNavigate(Screen.IncidentsHistory.route) }
                )

                // Center button - Check In/Out based on session state
                CircularMenuItem(
                    icon = if (sessionState.session != null) 
                           Icons.Default.Close 
                           else Icons.Default.PlayArrow,
                    label = if (sessionState.session != null) 
                           "Check Out" 
                           else "Check In",
                    onClick = {
                        if (sessionState.session != null) {
                            sessionViewModel.endSession()
                        } else {
                            onNavigate(Screen.QRScanner.route)
                        }
                    }
                )

                // Vehicles button
                CircularMenuItem(
                    icon = Icons.Default.Star,
                    label = "Vehicles",
                    onClick = { onNavigate(Screen.Vehicles.route) }
                )
            }

            // Activity button
            CircularMenuItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Activity",
                onClick = { onNavigate(Screen.OperatorsCICOHistory.route) }
            )
        }
    }
}

@Composable
private fun CircularMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(90.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black,
            contentColor = Color(0xFFFFA726) // Orange color from login screen
        ),
        border = BorderStroke(1.dp, Color(0xFFFFA726))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFFFA726)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = Color(0xFFFFA726)
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
            color = Color.White
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
