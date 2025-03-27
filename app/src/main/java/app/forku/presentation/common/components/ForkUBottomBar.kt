package app.forku.presentation.common.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import app.forku.presentation.navigation.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.core.AppIcons
import app.forku.presentation.common.viewmodel.BottomSheetViewModel
import app.forku.presentation.incident.components.IncidentTypeSelector
import app.forku.presentation.dashboard.DashboardState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.forku.domain.model.user.UserRole

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
    val userRole = dashboardState.user?.role ?: UserRole.OPERATOR

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
            val navItems = mutableListOf(
                BottomNavItem(
                    "Home", 
                    painterResource(id = AppIcons.General.home), 
                    route = if (userRole == UserRole.ADMIN) Screen.AdminDashboard.route else Screen.Dashboard.route
                )
            )

            if (userRole == UserRole.ADMIN) {
                navItems.add(BottomNavItem(
                    title = "Checklist",
                    icon = rememberVectorPainter(Icons.Default.CheckCircle),
                    route = Screen.AllChecklist.route
                ))
            }

            navItems.add(BottomNavItem(
                "Report",
                painterResource(id = AppIcons.General.addIncident),
                Screen.SafetyReporting.route
            ))

            if (userRole == UserRole.ADMIN) {
                navItems.add(
                    BottomNavItem(
                        "Profile",
                        rememberVectorPainter(Icons.Outlined.AccountCircle),
                        Screen.Profile.route
                    )
                )
            }

            if (userRole == UserRole.ADMIN) {
                navItems.add(BottomNavItem(
                    title = "Fleet",
                    icon = painterResource(id = AppIcons.General.forklift),
                    route = currentVehicleId?.let {
                        Screen.VehiclesList.route
                    } ?: Screen.VehiclesList.route
                ))
            } else if (userRole == UserRole.OPERATOR) {
                navItems.add(BottomNavItem(
                    title = "My Vehicle",
                    icon = painterResource(id = AppIcons.General.forklift),
                    route = currentVehicleId?.let {
                        Screen.VehicleProfile.route.replace("{vehicleId}", it)
                    } ?: Screen.VehiclesList.route,
                    enabled = currentVehicleId != null
                ))
            }


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
                    label = { Text(item.title, fontSize = 10.sp) },
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
