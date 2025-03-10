package app.forku.presentation.common.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.forku.presentation.navigation.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.core.AppIcons
import app.forku.presentation.common.viewmodel.BottomSheetViewModel
import app.forku.presentation.incident.components.IncidentTypeSelector
import app.forku.presentation.dashboard.DashboardState
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun ForkUBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BottomSheetViewModel = hiltViewModel(),
    currentVehicleId: String? = null,
    currentCheckId: String? = null,
    dashboardState: DashboardState
) {
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()

    Column {
        if (showBottomSheet) {
            AppBottomSheet(
                onDismiss = { viewModel.hideBottomSheet() },
                content = {
                    IncidentTypeSelector(
                        onTypeSelected = { type ->
                            navController.navigate("incident_report/$type") {
                                popUpTo("incident_report/$type") { inclusive = true }
                            }
                            viewModel.hideBottomSheet()
                        },
                        onDismiss = { viewModel.hideBottomSheet() }
                    )
                }
            )
        }

        NavigationBar(
            modifier = modifier,
            containerColor = Color(0xFF1B1F23),
            tonalElevation = 0.dp
        ) {
            val navItems = listOf(
                BottomNavItem("Home", painterResource(id = AppIcons.General.home), Screen.Dashboard.route),
                // BottomNavItem(
                //     title = "Checklist",
                //     icon = Icons.Default.CheckCircle,
                //     route = currentVehicleId?.let { 
                //         currentCheckId?.let { checkId ->
                //             "checklist/$currentVehicleId?checkId=$checkId"
                //         }
                //     } ?: Screen.Checklist.route,
                //     enabled = currentVehicleId != null && currentCheckId != null && 
                //               dashboardState.currentSession != null
                // ),
                BottomNavItem("Report", painterResource(id = AppIcons.General.addIncident), Screen.SafetyReporting.route),
                // BottomNavItem("Alerts", Icons.Default.Notifications, Screen.Notifications.route),

                //Icon(, "Cuenta")
                BottomNavItem(
                    title = "Vehicle",
                    icon = painterResource(id = AppIcons.General.forklift),
                    route = currentVehicleId?.let { 
                        Screen.VehicleProfile.route.replace("{vehicleId}", it) 
                    } ?: Screen.Vehicles.route,
                    enabled = currentVehicleId != null
                )

            )

            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

            navItems.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        when {
                            !item.enabled -> { /* Do nothing */ }
                            item.route == Screen.SafetyReporting.route -> {
                                viewModel.showBottomSheet()
                            }
                            item.route == Screen.Notifications.route -> {
                                // Do nothing for now
                            }
                            else -> {
                                navController.navigate(item.route) {
                                    popUpTo(item.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    enabled = item.enabled,
                    icon = {
                        Icon(
                            painter = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(item.title, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFBF00),
                        selectedTextColor = Color(0xFFFFBF00),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF1B1F23)
                    )
                )
            }
        }
    }
}

private data class BottomNavItem(
    val title: String,
    val icon: Painter,
    val route: String,
    val enabled: Boolean = true,
    val checkId: String? = null
)