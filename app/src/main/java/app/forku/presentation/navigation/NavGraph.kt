package app.forku.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.forku.presentation.user.login.LoginScreen
import app.forku.presentation.dashboard.DashboardScreen
import app.forku.presentation.checklist.ChecklistScreen
import app.forku.presentation.vehicle.list.VehicleListScreen
import app.forku.presentation.vehicle.profile.VehicleProfileScreen
import app.forku.presentation.vehicle.scanner.QRScannerScreen
import app.forku.presentation.vehicle.profile.VehicleProfileViewModel
import app.forku.presentation.checklist.ChecklistViewModel
import app.forku.presentation.incident.IncidentReportScreen
import app.forku.presentation.incident.list.IncidentListScreen
import app.forku.presentation.incident.IncidentReportViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object QRScanner : Screen("qr_scanner")
    data object VehicleProfile : Screen("vehicle_profile/{vehicleId}")
    data object Checklist : Screen("checklist/{vehicleId}")
    data object Profile : Screen("profile")
    data object IncidentsHistory : Screen("incidents_history")
    data object OperatorsCICOHistory : Screen("operators_cico_history")
    data object Vehicles : Screen("vehicles")
    data object SafetyReporting : Screen("safety_reporting")
    data object IncidentReport : Screen("incident_report")
    data object IncidentList : Screen("incident_list")
}

@Composable
fun ForkUNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(Screen.QRScanner.route) {
            QRScannerScreen(
                onNavigateToPreShiftCheck = { vehicleId ->
                    navController.navigate(Screen.Checklist.route.replace("{vehicleId}", vehicleId))
                },
                onNavigateToVehicleProfile = { vehicleId ->
                    navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleId))
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            Screen.Checklist.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: return@composable
            val viewModel: ChecklistViewModel = hiltViewModel()
            
            ChecklistScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = { 
                    navController.navigate(Screen.Dashboard.route) {
                        // Clear the back stack up to Dashboard
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Vehicles.route) {
            VehicleListScreen(
                onVehicleClick = { vehicleId ->
                    navController.navigate("vehicle_profile/$vehicleId")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = Screen.VehicleProfile.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { entry ->
            val vehicleId = entry.arguments?.getString("vehicleId") ?: return@composable
            val viewModel: VehicleProfileViewModel = hiltViewModel()
            VehicleProfileScreen(
                viewModel = viewModel,
                onComplete = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
                onPreShiftCheck = { vehicleId ->
                    navController.navigate(Screen.Checklist.route.replace("{vehicleId}", vehicleId))
                },
                onScanQrCode = {
                    navController.navigate(Screen.QRScanner.route)
                }
            )
        }

        composable(Screen.SafetyReporting.route) {
            // Remove direct navigation
            // Let the BottomSheet handle the type selection first
        }

        composable(
            route = "incident_report/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val incidentType = backStackEntry.arguments?.getString("type") ?: return@composable
            val viewModel: IncidentReportViewModel = hiltViewModel()
            
            IncidentReportScreen(
                incidentType = incidentType,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Screen.IncidentList.route) {
            IncidentListScreen(
                onNavigateToReport = { 
                    navController.navigate(Screen.IncidentReport.route)
                }
            )
        }
    }
}