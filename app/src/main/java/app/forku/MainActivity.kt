package app.forku

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
import app.forku.core.auth.SessionKeepAliveManager
import app.forku.core.utils.GooglePlayServicesHelper
import kotlinx.coroutines.delay

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

    @Inject
    lateinit var sessionKeepAliveManager: SessionKeepAliveManager

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

        // Check Google Play Services availability
        if (!GooglePlayServicesHelper.isGooglePlayServicesAvailable(this)) {
            val errorMessage = GooglePlayServicesHelper.getErrorMessage(this)
            Log.w("MainActivity", "Google Play Services issue: $errorMessage")
            // App can still function without Google Play Services for core features
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
                        // Stop session keep-alive when authentication fails
                        sessionKeepAliveManager.stopKeepAlive()
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

            // Manage session keep-alive based on authentication status
            LaunchedEffect(isAuthenticated) {
                // Run in IO dispatcher to avoid blocking main thread
                withContext(Dispatchers.IO) {
                    if (isAuthenticated) {
                        // Start keep-alive when user is authenticated
                        Log.d("MainActivity", "🚀 User authenticated, starting session keep-alive")
                        sessionKeepAliveManager.startKeepAlive()
                        
                        // Log status after a short delay to see if it started properly
                        delay(2000)
                        sessionKeepAliveManager.logSessionStatus()
                        
                        // 🚀 FOR TESTING: Force immediate execution after 5 seconds
                        delay(5000)
                        Log.d("MainActivity", "🧪 TESTING: Forcing immediate execution...")
                        sessionKeepAliveManager.forceExecuteNow()
                    } else {
                        // Stop keep-alive when user is not authenticated
                        Log.d("MainActivity", "🛑 User not authenticated, stopping session keep-alive")
                        sessionKeepAliveManager.stopKeepAlive()
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        // Clean up session keep-alive resources
        sessionKeepAliveManager.cleanup()
    }
    
    override fun onPause() {
        super.onPause()
        // App going to background - switch to background intervals
        Log.d("MainActivity", "🌙 App going to background")
        sessionKeepAliveManager.onAppGoesToBackground()
    }
    
    override fun onResume() {
        super.onResume()
        // App coming to foreground - validate session and switch to foreground intervals
        Log.d("MainActivity", "🌅 App coming to foreground")
        lifecycleScope.launch {
            val isSessionValid = sessionKeepAliveManager.onAppComesToForeground()
            if (!isSessionValid) {
                Log.w("MainActivity", "⚠️ Session validation failed - attempting emergency session check")
                // ✅ IMPROVED: Don't immediately clear auth, try emergency validation first
                val emergencyCheckPassed = sessionKeepAliveManager.performEmergencySessionCheck()
                if (!emergencyCheckPassed) {
                    Log.e("MainActivity", "❌ Emergency session check failed - session is truly expired")
                    authDataStore.clearAuth()
                    // Navigation will be handled by the authentication state observer
                } else {
                    Log.i("MainActivity", "✅ Emergency session check passed - session is still valid")
                    // Session is actually valid, continue normally
                }
            } else {
                Log.d("MainActivity", "✅ Session validation passed on foreground")
            }
        }
    }
}