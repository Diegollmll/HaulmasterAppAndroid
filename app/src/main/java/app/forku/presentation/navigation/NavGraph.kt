package app.forku.presentation.navigation

import app.forku.presentation.user.cico.CicoHistoryScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.user.login.LoginScreen
import app.forku.presentation.dashboard.DashboardScreen
import app.forku.presentation.checklist.ChecklistScreen
import app.forku.presentation.vehicle.list.VehicleListScreen
import app.forku.presentation.vehicle.profile.VehicleProfileScreen
import app.forku.presentation.scanner.QRScannerScreen
import app.forku.presentation.vehicle.profile.VehicleProfileViewModel
import app.forku.presentation.checklist.ChecklistViewModel
import app.forku.presentation.incident.IncidentReportScreen
import app.forku.presentation.incident.IncidentReportViewModel
import app.forku.presentation.incident.list.IncidentListScreen
import app.forku.presentation.user.profile.ProfileScreen
import app.forku.presentation.vehicle.manual.PerformanceReportScreen

import app.forku.presentation.incident.detail.IncidentDetailScreen
import app.forku.presentation.tour.TourScreen
import app.forku.presentation.user.register.RegisterScreen
import app.forku.presentation.dashboard.AdminDashboardScreen
import app.forku.presentation.dashboard.DashboardViewModel
import app.forku.presentation.user.login.LoginState
import app.forku.domain.model.user.UserRole
import app.forku.presentation.certification.list.CertificationsScreen
import app.forku.presentation.certification.detail.CertificationDetailScreen
import app.forku.presentation.certification.edit.CertificationEditScreen
import app.forku.presentation.certification.create.CertificationCreateScreen
import app.forku.presentation.checklist.AllChecklistScreen
import app.forku.presentation.notification.NotificationScreen
import app.forku.presentation.user.operator.OperatorsListScreen
import app.forku.presentation.checklist.CheckDetailScreen
import app.forku.presentation.safety.SafetyAlertsScreen



@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Tour.route,
    networkManager: NetworkConnectivityManager
) {
    val viewModel = hiltViewModel<DashboardViewModel>()
    val currentUser by viewModel.currentUser.collectAsState()
    val tourCompleted by viewModel.tourCompleted.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val hasToken by viewModel.hasToken.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when {
            !tourCompleted -> Screen.Tour.route
            loginState is LoginState.Success || hasToken -> {
                when (currentUser?.role) {
                    UserRole.ADMIN -> Screen.AdminDashboard.route
                    else -> Screen.Dashboard.route
                }
            }
            else -> Screen.Login.route
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    val route = when (user.role) {
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                networkManager = networkManager,
                navController = navController
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(Screen.Tour.route) {
            TourScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                networkManager = networkManager
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                networkManager = networkManager
            )
        }

        composable(Screen.QRScanner.route) {
            QRScannerScreen(
                onNavigateToPreShiftCheck = { vehicleId ->
                    navController.navigate("checklist/${vehicleId}?fromScanner=true")
                },
                onNavigateToVehicleProfile = { vehicleId ->
                    navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                networkManager = networkManager
            )
        }

        composable(
            route = Screen.Checklist.route + "?fromScanner={fromScanner}",
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("fromScanner") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: return@composable
            val fromScanner = backStackEntry.arguments?.getBoolean("fromScanner") ?: false
            val viewModel: ChecklistViewModel = hiltViewModel()
            
            ChecklistScreen(
                viewModel = viewModel,
                navController = navController,
                onBackPressed = {
                    if (fromScanner) {
                        navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleId)) {
                            popUpTo(Screen.QRScanner.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                networkManager = networkManager
            )
        }

        composable(Screen.VehiclesList.route) {
            VehicleListScreen(
                navController = navController,
                onVehicleClick = { vehicleId ->
                    navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleId))
                },
                networkManager = networkManager
            )
        }

        composable(
            route = Screen.VehicleProfile.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { entry ->
            val vehicleId = entry.arguments?.getString("vehicleId") ?: return@composable
            val viewModel: VehicleProfileViewModel = hiltViewModel()
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            VehicleProfileScreen(
                viewModel = viewModel,
                onComplete = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
                onPreShiftCheck = { vehicleId ->
                    navController.navigate(Screen.Checklist.route.replace("{vehicleId}", vehicleId))
                },
                onScanQrCode = {
                    navController.navigate(Screen.QRScanner.route)
                },
                navController = navController,
                networkManager = networkManager,
                userRole = currentUser?.role ?: UserRole.OPERATOR
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
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("operatorId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId")
            android.util.Log.e("appflow", "NavGraph composable(route = Screen.Profile.route, operatorId: $operatorId")
            ProfileScreen(
                navController = navController,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCicoHistory = { 
                    navController.navigate(Screen.OperatorsCICOHistory.createRoute(operatorId))
                },
                networkManager = networkManager,
                operatorId = operatorId
            )
        }

        composable(
            route = Screen.OperatorsCICOHistory.route,
            arguments = listOf(
                navArgument("operatorId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("source") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val operatorId = backStackEntry.arguments?.getString("operatorId")
            val source = backStackEntry.arguments?.getString("source")
            android.util.Log.e("appflow", "NavGraph composable(route = Screen.OperatorsCICOHistory.route operatorId: $operatorId, source: $source")
            CicoHistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                navController = navController,
                networkManager = networkManager,
                operatorId = operatorId,
                source = source
            )
        }

        composable(
            route = Screen.IncidentList.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("source") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val source = backStackEntry.arguments?.getString("source")
            
            IncidentListScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToReport = { /* Handle report navigation */ },
                navController = navController,
                networkManager = networkManager,
                userId = userId,
                source = source
            )
        }

        composable(Screen.PerformanceReport.route) {
            PerformanceReportScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(
            route = Screen.IncidentDetail.route,
            arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId") ?: return@composable
            IncidentDetailScreen(
                incidentId = incidentId,
                navController = navController,
                networkManager = networkManager
            )
        }


        composable(Screen.OperatorsList.route) {
            OperatorsListScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(Screen.AllChecklist.route) {
            AllChecklistScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(
            route = Screen.CheckDetail.route,
            arguments = listOf(
                navArgument("checkId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val checkId = backStackEntry.arguments?.getString("checkId") ?: return@composable
            CheckDetailScreen(
                checkId = checkId,
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(Screen.SafetyAlerts.route) {
            SafetyAlertsScreen(
                navController = navController,
                networkManager = networkManager
            )
        }

        // Certifications routes
        composable(
            route = Screen.CertificationsList.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            CertificationsScreen(
                navController = navController,
                networkManager = networkManager,
                userId = userId
            )
        }

        composable(
            route = Screen.CertificationDetail.route,
            arguments = listOf(
                navArgument("certificationId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val certificationId = backStackEntry.arguments?.getString("certificationId") 
                ?: return@composable
            CertificationDetailScreen(
                certificationId = certificationId,
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(
            route = Screen.CertificationEdit.route,
            arguments = listOf(
                navArgument("certificationId") { 
                    type = NavType.StringType 
                }
            )
        ) { backStackEntry ->
            val certificationId = backStackEntry.arguments?.getString("certificationId") 
                ?: return@composable
            CertificationEditScreen(
                certificationId = certificationId,
                navController = navController,
                networkManager = networkManager
            )
        }

        composable(Screen.CertificationCreate.route) {
            CertificationCreateScreen(
                navController = navController,
                networkManager = networkManager
            )
        }
    }
}