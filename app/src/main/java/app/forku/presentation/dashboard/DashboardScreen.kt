package app.forku.presentation.dashboard

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
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.presentation.user.login.LoginState
import app.forku.presentation.user.login.LoginViewModel
import app.forku.presentation.common.components.ForkUBottomBar
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.LoadingScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController = rememberNavController(),
    onNavigate: (String) -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by dashboardViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.background(Color.Black),
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (dashboardState.vehicleStatus) {
                            VehicleStatus.CHECKED_OUT -> "You are checked-out"
                            VehicleStatus.CHECKED_IN -> "You are checked-in"
                            VehicleStatus.IN_USE -> "You are using this vehicle"
                            VehicleStatus.BLOCKED -> "Vehicle in use by another driver"
                            VehicleStatus.UNKNOWN -> "Checking vehicle status..."
                        },
                        color = Color.White
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                dashboardState.isLoading -> LoadingOverlay()
                dashboardState.error != null -> ErrorScreen(
                    message = dashboardState.error!!,
                    onRetry = { dashboardViewModel.loadDashboardStatus() }
                )
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Circular Menu
                        CircularMenu(
                            onNavigate = onNavigate,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularMenu(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Top button (Profile)
            CircularMenuItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = { onNavigate(Screen.Profile.route) }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left button (Incidents)
                CircularMenuItem(
                    icon = Icons.Default.Search,
                    label = "Incidents",
                    onClick = { onNavigate(Screen.IncidentsHistory.route) }
                )

                // Center button (Check In - Check out)
                CircularMenuItem(
                    icon = Icons.Default.PlayArrow,
                    label = "Check In",
                    onClick = { onNavigate(Screen.QRScanner.route) }
                )

                // Right button (Vehicles)
                CircularMenuItem(
                    icon = Icons.Default.Star,
                    label = "Vehicles",
                    onClick = { onNavigate(Screen.Vehicles.route) }
                )
            }

            // Bottom button (Operators)
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