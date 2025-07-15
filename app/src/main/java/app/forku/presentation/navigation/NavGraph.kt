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
import app.forku.core.location.LocationManager
import app.forku.presentation.user.login.LoginScreen
import app.forku.presentation.dashboard.DashboardScreen
import app.forku.presentation.dashboard.SuperAdminDashboardScreen
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
import app.forku.domain.model.user.UserRole
import app.forku.presentation.certification.CertificationScreen
import app.forku.presentation.certification.list.CertificationsScreen
import app.forku.presentation.certification.detail.CertificationDetailScreen
import app.forku.presentation.checklist.AllChecklistScreen
import app.forku.presentation.notification.NotificationScreen
import app.forku.presentation.user.operator.OperatorsListScreen
import app.forku.presentation.checklist.CheckDetailScreen
import app.forku.presentation.safety.SafetyAlertsScreen
import app.forku.presentation.business.BusinessManagementScreen
import app.forku.presentation.dashboard.SystemOwnerDashboardScreen
import app.forku.presentation.user.management.UserManagementScreen
import app.forku.presentation.system.SystemSettingsScreen
import app.forku.presentation.system.UserPreferencesSetupScreen
import app.forku.presentation.countries.CountriesScreen
import app.forku.presentation.timezones.TimeZonesScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import app.forku.presentation.checklist.manage_checklist.ManageChecklistScreen
import app.forku.presentation.checklist.item.QuestionsChecklistItemScreen
import androidx.compose.runtime.LaunchedEffect
import app.forku.presentation.vehicle.add.AddVehicleScreen
import app.forku.presentation.vehicle.category.VehicleCategoryScreen
import app.forku.presentation.vehicle.type.VehicleTypeScreen
import app.forku.presentation.vehicle.edit.EditVehicleScreen
import app.forku.presentation.admin.vehicle.AdminVehiclesListScreen
import app.forku.presentation.admin.vehicle.AdminVehicleProfileScreen
import androidx.compose.material3.Text

import app.forku.presentation.checklist.subcategory.ChecklistSubcategoriesScreen
import androidx.compose.runtime.remember
import app.forku.presentation.checklist.manage_checklist.ManageChecklistViewModel
import app.forku.presentation.system.EnergySourcesScreen
import app.forku.presentation.sites.SitesScreen
import app.forku.presentation.vehicle.component.VehicleComponentsScreen
import app.forku.presentation.gogroup.GroupManagementScreen
import app.forku.presentation.gogroup.GroupRoleManagementScreen
import app.forku.presentation.report.ReportScreen
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.auth.AuthenticationState
import coil.ImageLoader

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NavGraphEntryPoint {
    fun businessContextManager(): app.forku.core.business.BusinessContextManager
}


@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Tour.route,
    networkManager: NetworkConnectivityManager,
    locationManager: LocationManager,
    userRole: UserRole,
    isAuthenticated: Boolean,
    tourCompleted: Boolean,
    tokenErrorHandler: TokenErrorHandler,
    imageLoader: ImageLoader
) {
    val viewModel = hiltViewModel<DashboardViewModel>()
    val tourCompletedState by viewModel.tourCompleted.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val hasToken by viewModel.hasToken.collectAsState()
    val authState by tokenErrorHandler.authenticationState.collectAsState()

    // ✅ NEW: Business context validation for authenticated users
    val context = androidx.compose.ui.platform.LocalContext.current
    val businessContextManager = remember { 
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            context.applicationContext,
            NavGraphEntryPoint::class.java
        ).businessContextManager()
    }
    val businessContextState by businessContextManager.contextState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthenticationState.RequiresAuthentication) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && 
                !currentRoute.contains(Screen.Login.route) && 
                !currentRoute.contains(Screen.Register.route)) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // ✅ NEW: Validate user preferences after successful authentication
    LaunchedEffect(isAuthenticated, hasToken) {
        android.util.Log.d("NavGraph", "[FLOW] LaunchedEffect(isAuthenticated, hasToken) - isAuthenticated: $isAuthenticated, hasToken: $hasToken")
        
        if (isAuthenticated && hasToken) {
            try {
                android.util.Log.d("NavGraph", "[FLOW] Both authenticated and has token - starting validation")
                
                // Load business context to check preferences
                android.util.Log.d("NavGraph", "[FLOW] About to call businessContextManager.loadBusinessContext()")
                businessContextManager.loadBusinessContext()
                android.util.Log.d("NavGraph", "[FLOW] Business context loading completed")
                
                // Check if user has valid preferences (both BusinessId AND SiteId)
                val hasValidPreferences = businessContextManager.hasValidUserPreferences()
                val currentRoute = navController.currentDestination?.route
                
                android.util.Log.d("NavGraph", "[FLOW] === PREFERENCES VALIDATION ===")
                android.util.Log.d("NavGraph", "[FLOW] IsAuthenticated: $isAuthenticated")
                android.util.Log.d("NavGraph", "[FLOW] HasToken: $hasToken")
                android.util.Log.d("NavGraph", "[FLOW] HasValidPreferences: $hasValidPreferences")
                android.util.Log.d("NavGraph", "[FLOW] CurrentRoute: $currentRoute")
                android.util.Log.d("NavGraph", "[FLOW] BusinessId: ${businessContextState.businessId}")
                android.util.Log.d("NavGraph", "[FLOW] SiteId: ${businessContextState.siteId}")
                
                // If user doesn't have valid preferences, redirect to UserPreferencesSetup
                if (!hasValidPreferences && 
                    currentRoute != null && 
                    !currentRoute.contains(Screen.UserPreferencesSetup.route) &&
                    !currentRoute.contains(Screen.SystemSettings.route) &&
                    !currentRoute.contains(Screen.Login.route) &&
                    !currentRoute.contains(Screen.Register.route) &&
                    !currentRoute.contains(Screen.Tour.route)) {
                    
                    android.util.Log.d("NavGraph", "[FLOW] ❌ User needs to set preferences, redirecting to UserPreferencesSetup")
                    navController.navigate(Screen.UserPreferencesSetup.createRoute(showBack = false)) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else if (hasValidPreferences) {
                    android.util.Log.d("NavGraph", "[FLOW] ✅ User has valid preferences")
                }
            } catch (e: Exception) {
                android.util.Log.e("NavGraph", "[FLOW] ❌ Error validating user preferences", e)
                android.util.Log.e("NavGraph", "[FLOW] Error details: ${e.message}")
                android.util.Log.e("NavGraph", "[FLOW] Error cause: ${e.cause}")
            }
        } else {
            android.util.Log.d("NavGraph", "[FLOW] ❌ Conditions not met - isAuthenticated: $isAuthenticated, hasToken: $hasToken")
        }
    }

    NavHost(
        navController = navController,
        startDestination = determineStartDestination(isAuthenticated, userRole, tourCompleted)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    android.util.Log.d("NavGraph", "[FLOW] onLoginSuccess called - user: $user, role: ${user.role}")
                    val route = when (user.role) {
                        UserRole.SYSTEM_OWNER -> Screen.SystemOwnerDashboard.route
                        UserRole.SUPERADMIN -> Screen.SuperAdminDashboard.route
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                        else -> Screen.Dashboard.route
                    }
                    android.util.Log.d("NavGraph", "[FLOW] Navigating to route: $route for user role: ${user.role}")
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                networkManager = networkManager,
                navController = navController,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.Tour.route) {
            TourScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler,
                imageLoader = imageLoader
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                networkManager = networkManager,
                imageLoader = imageLoader,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.QRScanner.route) {
            QRScannerScreen(
                onNavigateToPreShiftCheck = { vehicleId ->
                    navController.navigate(Screen.Checklist.createRoute(vehicleId, fromScanner = true))
                },
                onNavigateToVehicleProfile = { vehicleId ->
                    navController.navigate(Screen.VehicleProfile.route.replace("{vehicleId}", vehicleId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                networkManager = networkManager,
                navController = navController,
                viewModel = hiltViewModel(),
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(
            route = Screen.Checklist.route + "?checkId={checkId}&fromScanner={fromScanner}",
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("checkId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("fromScanner") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: return@composable
            val checkId = backStackEntry.arguments?.getString("checkId")
            val fromScanner = backStackEntry.arguments?.getBoolean("fromScanner") ?: false
            val viewModel: ChecklistViewModel = hiltViewModel()
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            ChecklistScreen(
                viewModel = viewModel,
                navController = navController,
                onBackPressed = {
                    if (fromScanner) {
                        navController.navigate(Screen.QRScanner.route) {
                            popUpTo(Screen.QRScanner.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                networkManager = networkManager,
                locationManager = locationManager,
                imageLoader = imageLoader,
                tokenErrorHandler = tokenErrorHandler,
                userRole = currentUser?.role ?: UserRole.OPERATOR
            )
        }

        composable(Screen.VehiclesList.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            VehicleListScreen(
                navController = navController,
                onVehicleClick = { vehicle ->
                    navController.navigate(
                        Screen.VehicleProfile.createRoute(
                            vehicleId = vehicle.id,
                            businessId = vehicle.businessId
                        )
                    )
                },
                networkManager = networkManager,
                userRole = currentUser?.role ?: UserRole.OPERATOR,
                imageLoader = imageLoader,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(
            route = Screen.VehicleProfile.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("businessId") { 
                    type = NavType.StringType 
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: return@composable
            val businessId = backStackEntry.arguments?.getString("businessId")
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
                userRole = currentUser?.role ?: UserRole.OPERATOR,
                imageLoader = imageLoader,
                tokenErrorHandler = tokenErrorHandler
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
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
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
            ProfileScreen(
                navController = navController,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCicoHistory = { 
                    navController.navigate(Screen.OperatorsCICOHistory.createRoute(operatorId))
                },
                networkManager = networkManager,
                operatorId = operatorId,
                tokenErrorHandler = tokenErrorHandler,
                imageLoader = imageLoader
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
            CicoHistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                navController = navController,
                networkManager = networkManager,
                operatorId = operatorId,
                source = source,
                tokenErrorHandler = tokenErrorHandler
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
                source = source,
                tokenErrorHandler = tokenErrorHandler,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.PerformanceReport.route) {
            PerformanceReportScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
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
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.OperatorsList.route) {
            OperatorsListScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.AllChecklist.route) {
            AllChecklistScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
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
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.SafetyAlerts.route) {
            SafetyAlertsScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
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
                tokenErrorHandler = tokenErrorHandler,
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
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
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
            CertificationScreen(
                certificationId = certificationId,
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.CertificationCreate.route) {
            CertificationScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.SuperAdminDashboard.route) {
            SuperAdminDashboardScreen(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.SystemOwnerDashboard.route) {
            SystemOwnerDashboardScreen(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route)
                },
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        // SuperAdmin specific routes
        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                navController = navController,
                networkManager = networkManager,
                showAddUserDialogByDefault = false,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.BusinessManagement.route) {
            BusinessManagementScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.RoleManagement.route) {
            // TODO: Implement RoleManagementScreen
        }

        composable(Screen.PermissionsManagement.route) {
            // TODO: Implement PermissionsManagementScreen
        }

        composable(Screen.AddUser.route) {
            UserManagementScreen(
                navController = navController,
                networkManager = networkManager,
                showAddUserDialogByDefault = true,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.AdminManagement.route) {
            // TODO: Implement AdminManagementScreen
        }

        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("businessId") { 
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            EditVehicleScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.MaintenanceSchedule.route) {
            // TODO: Implement MaintenanceScheduleScreen
        }

        composable(Screen.VehicleReports.route) {
            // TODO: Implement VehicleReportsScreen
        }

        composable(Screen.SystemSettings.route) {
            SystemSettingsScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(
            route = Screen.UserPreferencesSetup.route,
            arguments = listOf(navArgument("showBack") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val showBack = backStackEntry.arguments?.getBoolean("showBack") ?: false
            UserPreferencesSetupScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler,
                userRoleManager = app.forku.core.auth.UserRoleManager,
                showBackButton = showBack
                // onSetupComplete will be handled automatically based on user role
            )
        }

        composable(Screen.EnergySources.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                EnergySourcesScreen(
                    navController = navController,
                    networkManager = networkManager,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        composable(Screen.SystemBackup.route) {
            // TODO: Implement SystemBackupScreen
        }

        composable(Screen.TimeZones.route) {
            TimeZonesScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.Countries.route) {
            CountriesScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(Screen.VehicleCategories.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                VehicleCategoryScreen(
                    navController = navController,
                    networkManager = networkManager,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        composable(Screen.VehicleTypes.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                VehicleTypeScreen(
                    navController = navController,
                    networkManager = networkManager,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        composable(Screen.VehicleComponents.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                VehicleComponentsScreen(
                    navController = navController,
                    networkManager = networkManager,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        // ---- Admin Vehicle Routes ----
        composable(Screen.AdminVehiclesList.route) {
            AdminVehiclesListScreen(
                navController = navController,
                networkManager = networkManager,
                userRole = userRole,
                imageLoader = imageLoader,
                tokenErrorHandler = tokenErrorHandler
            )
        }
        composable(
            route = Screen.AdminVehicleProfile.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
             val vehicleId = backStackEntry.arguments?.getString("vehicleId")
             if (vehicleId != null) {
                 AdminVehicleProfileScreen(
                     vehicleId = vehicleId,
                     navController = navController,
                     networkManager = networkManager,
                     userRole = userRole,
                     tokenErrorHandler = tokenErrorHandler
                 )
             } else {
                 Text("Error: Vehicle ID missing")
             }
        }
        // ---- End Admin Vehicle Routes ----

        // TODO: Re-implement category management with proper ChecklistItem system
        // composable(Screen.ChecklistCategories.route) {
        //     val dashboardViewModel: DashboardViewModel = hiltViewModel()
        //     val currentUser by dashboardViewModel.currentUser.collectAsState()
        //     
        //     if (currentUser?.role == UserRole.SYSTEM_OWNER) {
        //         // ChecklistItemCategoryScreen - to be implemented
        //         Text("Category management temporarily unavailable")
        //     } else {
        //         LaunchedEffect(Unit) {
        //             navController.navigateUp()
        //         }
        //     }
        // }

        // TODO: Re-implement subcategory management with proper ChecklistItem system
        // composable(
        //     route = Screen.QuestionaryChecklistItemSubcategory.route,
        //     arguments = listOf(
        //         navArgument("categoryId") { type = NavType.StringType }
        //     )
        // ) { backStackEntry ->
        //     val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
        //     val dashboardViewModel: DashboardViewModel = hiltViewModel()
        //     val currentUser by dashboardViewModel.currentUser.collectAsState()
        //     
        //     if (currentUser?.role == UserRole.SYSTEM_OWNER) {
        //         // ChecklistItemSubcategoryScreen - to be implemented
        //         Text("Subcategory management temporarily unavailable")
        //     } else {
        //         LaunchedEffect(Unit) {
        //             navController.navigateUp()
        //         }
        //     }
        // }

        composable(Screen.ChecklistSubcategories.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                ChecklistSubcategoriesScreen(
                    navController = navController,
                    networkManager = networkManager,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        // Añadimos la ruta para Questionary
        composable(Screen.Questionnaires.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            // ✅ DEBUG: Add detailed logging for role verification
            android.util.Log.d("NavGraph", "=== QUESTIONNAIRES ROUTE ACCESS DEBUG ===")
            android.util.Log.d("NavGraph", "Current user: ${currentUser?.firstName} ${currentUser?.lastName}")
            android.util.Log.d("NavGraph", "User ID: ${currentUser?.id}")
            android.util.Log.d("NavGraph", "User role: ${currentUser?.role}")
            android.util.Log.d("NavGraph", "User role name: ${currentUser?.role?.name}")
            android.util.Log.d("NavGraph", "Is SYSTEM_OWNER? ${currentUser?.role == UserRole.SYSTEM_OWNER}")
            android.util.Log.d("NavGraph", "Is SUPERADMIN? ${currentUser?.role == UserRole.SUPERADMIN}")
            android.util.Log.d("NavGraph", "Is ADMIN? ${currentUser?.role == UserRole.ADMIN}")
            android.util.Log.d("NavGraph", "Has valid role? ${currentUser?.role == UserRole.SYSTEM_OWNER || currentUser?.role == UserRole.SUPERADMIN || currentUser?.role == UserRole.ADMIN}")
            android.util.Log.d("NavGraph", "==========================================")
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER || currentUser?.role == UserRole.SUPERADMIN || currentUser?.role == UserRole.ADMIN) {
                android.util.Log.d("NavGraph", "✅ Access granted - showing ManageChecklistScreen")
                ManageChecklistScreen(
                    navController = navController,
                    networkManager = networkManager,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                android.util.Log.w("NavGraph", "❌ Access denied - navigating back")
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        // Añadimos la ruta para QuestionaryItems
        composable(
            route = Screen.QuestionaryItems.route,
            arguments = listOf(
                navArgument("questionaryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("isEditable") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            val questionaryId = backStackEntry.arguments?.getString("questionaryId")
            val isEditable = backStackEntry.arguments?.getBoolean("isEditable") ?: true
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            val manageChecklistViewModel: ManageChecklistViewModel = hiltViewModel()

            if (currentUser?.role == UserRole.SYSTEM_OWNER || currentUser?.role == UserRole.SUPERADMIN || currentUser?.role == UserRole.ADMIN) {
                if (questionaryId != null) {
                    QuestionsChecklistItemScreen(
                        navController = navController,
                        networkManager = networkManager,
                        checklistId = questionaryId,
                        isEditable = isEditable,
                        tokenErrorHandler = tokenErrorHandler
                    )
                } else {
                    ManageChecklistScreen(
                        navController = navController,
                        networkManager = networkManager,
                        tokenErrorHandler = tokenErrorHandler
                    )
                }
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        // Add Sites Management Route
        composable(
            route = Screen.Sites.route,
            arguments = listOf(
                navArgument("businessId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: return@composable
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            if (currentUser?.role == UserRole.SYSTEM_OWNER || 
                (currentUser?.role == UserRole.SUPERADMIN && currentUser?.id == businessId)) {
                SitesScreen(
                    navController = navController,
                    networkManager = networkManager,
                    businessId = businessId,
                    tokenErrorHandler = tokenErrorHandler
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigateUp()
                }
            }
        }

        composable(Screen.GroupManagement.route) {
            GroupManagementScreen(
                onNavigateToRoles = { groupName ->
                    navController.navigate(Screen.GroupRoleManagement.createRoute(groupName))
                },
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        composable(
            route = Screen.GroupRoleManagement.route,
            arguments = listOf(
                navArgument(Screen.GroupRoleManagement.GROUP_NAME_ARG) {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val groupName = entry.arguments?.getString(Screen.GroupRoleManagement.GROUP_NAME_ARG) ?: return@composable
            GroupRoleManagementScreen(
                groupName = groupName,
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
        }

        // Reports Module - Admin/SuperAdmin only (TEMPORARILY OPEN FOR DEBUGGING)
        composable(Screen.Reports.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            
            android.util.Log.d("NavGraph", "Reports route accessed - currentUser: ${currentUser?.firstName}, role: ${currentUser?.role}")
            
            // TEMPORARILY ALLOW ALL USERS FOR DEBUGGING
            android.util.Log.d("NavGraph", "Allowing all users to access Reports for debugging")
            ReportScreen(
                navController = navController,
                networkManager = networkManager,
                tokenErrorHandler = tokenErrorHandler
            )
            
            // ORIGINAL CODE (commented out for debugging):
            // if (currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.SUPERADMIN) {
            //     android.util.Log.d("NavGraph", "User has valid role for Reports, showing ReportScreen")
            //     ReportScreen(
            //         navController = navController,
            //         networkManager = networkManager,
            //         tokenErrorHandler = tokenErrorHandler
            //     )
            // } else {
            //     android.util.Log.w("NavGraph", "User does not have valid role for Reports (${currentUser?.role}), navigating back")
            //     LaunchedEffect(Unit) {
            //         navController.navigateUp()
            //     }
            // }
        }
    }
}

private fun determineStartDestination(
    isAuthenticated: Boolean,
    userRole: UserRole?,
    tourCompleted: Boolean
): String {
    return when {
        !tourCompleted -> Screen.Tour.route
        isAuthenticated -> {
             when (userRole) {
                 UserRole.SYSTEM_OWNER -> Screen.SystemOwnerDashboard.route
                 UserRole.SUPERADMIN -> Screen.SuperAdminDashboard.route
                 UserRole.ADMIN -> Screen.AdminDashboard.route
                 UserRole.OPERATOR -> Screen.Dashboard.route
                 else -> Screen.Login.route
             }
         }
        else -> Screen.Login.route
    }
}