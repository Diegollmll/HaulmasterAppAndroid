package app.forku.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.forku.presentation.auth.login.LoginScreen
import app.forku.presentation.dashboard.DashboardScreen
import app.forku.presentation.vehicle.checklist.ChecklistScreen
import app.forku.presentation.vehicle.list.VehicleListScreen
import app.forku.presentation.vehicle.profile.VehicleProfileScreen
import app.forku.presentation.vehicle.scanner.QRScannerScreen

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
                onQrCodeScanned = { qrCode ->
                    navController.navigate("vehicle_profile/$qrCode")
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
            ChecklistScreen(
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
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
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")
                ?: return@composable
            VehicleProfileScreen(
                viewModel = hiltViewModel(),
                onStartCheck = {
                    navController.navigate("checklist/$vehicleId")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }


    }
}