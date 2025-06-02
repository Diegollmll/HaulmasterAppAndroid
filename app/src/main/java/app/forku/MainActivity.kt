package app.forku

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.forku.data.datastore.AuthDataStore
import app.forku.data.local.TourPreferences
import app.forku.presentation.user.login.LoginState
import app.forku.presentation.user.login.LoginViewModel
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.LoadingScreen
import app.forku.presentation.common.theme.ForkUTheme
import app.forku.presentation.navigation.NavGraph
import app.forku.presentation.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.view.View
import androidx.compose.ui.graphics.toArgb
import app.forku.presentation.common.theme.BackgroundGray
import androidx.lifecycle.lifecycleScope
import app.forku.core.network.NetworkConnectivityManager
import kotlinx.coroutines.launch
import app.forku.core.location.LocationManager
import app.forku.presentation.dashboard.DashboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.user.UserRole
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import app.forku.core.auth.AuthenticationState
import app.forku.core.auth.TokenErrorHandler
import kotlinx.coroutines.flow.collectLatest
import coil.ImageLoader
import app.forku.presentation.common.imageloader.LocalAuthenticatedImageLoader
import androidx.compose.runtime.CompositionLocalProvider

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authDataStore: AuthDataStore

    @Inject
    lateinit var tourPreferences: TourPreferences

    @Inject
    lateinit var networkManager: NetworkConnectivityManager

    @Inject
    lateinit var locationManager: LocationManager
    
    @Inject
    lateinit var tokenErrorHandler: TokenErrorHandler

    @Inject
    lateinit var imageLoader: ImageLoader

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = BackgroundGray.toArgb()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        super.onCreate(savedInstanceState)
        
        // Initialize token at startup
        lifecycleScope.launch {
            authDataStore.initializeApplicationToken()
        }

        setContent {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val currentUser by dashboardViewModel.currentUser.collectAsState()
            val userRole = currentUser?.role
            val isAuthenticated by dashboardViewModel.hasToken.collectAsState()
            val tourCompleted by dashboardViewModel.tourCompleted.collectAsState()
            
            val loginState by loginViewModel.state.collectAsState()
            val navController = rememberNavController()
            
            // State to track if we're showing an auth error
            val authErrorMessage = remember { mutableStateOf<String?>(null) }
            val showAuthModal = remember { mutableStateOf(false) }
            // Helper to filter error messages
            fun filterAuthErrorMessage(raw: String?): String {
                if (raw == null) return "Authentication required. Please log in again."
                val lower = raw.lowercase()
                return when {
                    lower.contains("expiredsecuritytoken") || lower.contains("session expired") ->
                        "Your session has expired. Please log in again."
                    lower.contains("not authorized") || lower.contains("forbidden access") ->
                        "You are not authorized to access this resource. Please log in again."
                    lower.contains("authentication required") || lower.contains("invalid token") ->
                        "Authentication required. Please log in again."
                    else -> "Authentication error. Please log in again."
                }
            }
            
            // Observe authentication state in composition
            val authState by tokenErrorHandler.authenticationState.collectAsState()
            
            // Watch for authentication events that require logging out
            LaunchedEffect(authState) {
                when (val state = authState) {
                    is AuthenticationState.RequiresAuthentication -> {
                        // Show toast and redirect to login, no modal
                        Toast.makeText(this@MainActivity, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show()
                        lifecycleScope.launch { authDataStore.clearAuth() }
                        if (navController.currentDestination?.route != Screen.Login.route) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                    else -> { /* do nothing */ }
                }
            }

            ForkUTheme {
                CompositionLocalProvider(LocalAuthenticatedImageLoader provides imageLoader) {
                    // Only show the modal for login errors, not for session expiration
                    if (showAuthModal.value && authErrorMessage.value != null && loginState is LoginState.Error) {
                        app.forku.presentation.common.components.AppModal(
                            onDismiss = {
                                showAuthModal.value = false
                                authErrorMessage.value = null
                                // Clear auth data
                                lifecycleScope.launch { authDataStore.clearAuth() }
                                // Navigate to login screen
                                if (navController.currentDestination?.route != Screen.Login.route) {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = false }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            },
                            onConfirm = {
                                showAuthModal.value = false
                                authErrorMessage.value = null
                                // Clear auth data
                                lifecycleScope.launch { authDataStore.clearAuth() }
                                // Navigate to login screen
                                if (navController.currentDestination?.route != Screen.Login.route) {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = false }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                }
                            },
                            title = "Authentication Required",
                            message = authErrorMessage.value ?: "Session expired. Please log in again.",
                            confirmText = "Go to Login",
                            dismissText = "Cancel"
                        )
                    }
                    when (loginState) {
                        is LoginState.Loading -> LoadingScreen()
                        is LoginState.Error -> {
                            val error = (loginState as LoginState.Error).message
                            ErrorScreen(
                                message = error,
                                onRetry = { loginViewModel.resetState() }
                            )
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            // Reset auth state when login succeeds
                            if (loginState is LoginState.Success) {
                                LaunchedEffect(Unit) {
                                    tokenErrorHandler.resetAuthenticationState()
                                }
                            }
                            
                            NavGraph(
                                navController = navController,
                                networkManager = networkManager,
                                locationManager = locationManager,
                                userRole = userRole ?: UserRole.OPERATOR,
                                isAuthenticated = isAuthenticated,
                                tourCompleted = tourCompleted,
                                tokenErrorHandler = tokenErrorHandler,
                                imageLoader = imageLoader
                            )
                        }
                    }
                }
            }
        }
    }
}