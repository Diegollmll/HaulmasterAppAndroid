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
            
            // Observe authentication state in composition
            val authState by tokenErrorHandler.authenticationState.collectAsState()
            
            // Watch for authentication events that require logging out
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthenticationState.RequiresAuthentication -> {
                        // Set error message and show it
                        val message = (authState as AuthenticationState.RequiresAuthentication).message
                        Toast.makeText(
                            this@MainActivity,
                            "Session expired: $message. Please log in again.",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Clear auth data
                        authDataStore.clearAuth()
                        
                        // Navigate to login screen
                        if (navController.currentDestination?.route != Screen.Login.route) {
                            navController.navigate(Screen.Login.route) {
                                // Pop up to the start destination to clean up the back stack
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = false
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when navigating back
                                restoreState = false
                            }
                        }
                    }
                    AuthenticationState.Authenticated -> {
                        // Clear any error message
                        authErrorMessage.value = null
                    }
                }
            }

            ForkUTheme {
                when (loginState) {
                    is LoginState.Loading -> LoadingScreen()
                    is LoginState.Error -> {
                        val error = (loginState as LoginState.Error).message
                        ErrorScreen(
                            message = error,
                            onRetry = { loginViewModel.resetState() }
                        )
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
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