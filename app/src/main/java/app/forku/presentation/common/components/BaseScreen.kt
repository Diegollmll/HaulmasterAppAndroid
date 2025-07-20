package app.forku.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.theme.BackgroundGray
import app.forku.presentation.dashboard.DashboardState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.surfaceColorAtElevation
import app.forku.domain.model.user.UserRole
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.auth.AuthenticationState
import app.forku.presentation.navigation.Screen
import app.forku.core.auth.SessionKeepAliveManager
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.forku.data.datastore.AuthDataStore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.colorResource

/**
 * Entry point for accessing SessionKeepAliveManager in Compose
 */
@EntryPoint
@InstallIn(ActivityComponent::class)
interface BaseScreenEntryPoint {
    fun sessionKeepAliveManager(): SessionKeepAliveManager
    fun authDataStore(): AuthDataStore
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = false,
    showBackButton: Boolean = true,
    currentVehicleId: String? = null,
    currentCheckId: String? = null,
    dashboardState: DashboardState? = null,
    viewModel: ViewModel? = null,
    topBarTitle: String? = null,
    onAppResume: (() -> Unit)? = null, // ✅ RENAMED: More descriptive - executes when app comes to foreground
    showLoadingOnRefresh: Boolean = false,
    networkManager: NetworkConnectivityManager,
    topBarActions: @Composable (() -> Unit)? = null,
    tokenErrorHandler: TokenErrorHandler,
    enableSessionKeepAlive: Boolean = true, // ✅ NEW: Allow disabling for login screens
    content: @Composable (PaddingValues) -> Unit
) {
    val TAG = "SESSION_FLOW"
    
    // ✅ NEW: Get SessionKeepAliveManager using EntryPoint
    val context = LocalContext.current
    val sessionKeepAliveManager = remember {
        if (enableSessionKeepAlive) {
            try {
                (context as? ComponentActivity)?.let { activity ->
                    EntryPointAccessors.fromActivity(
                        activity,
                        BaseScreenEntryPoint::class.java
                    ).sessionKeepAliveManager()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not access SessionKeepAliveManager", e)
                null
            }
        } else {
            null
        }
    }
    
    // ✅ NEW: Get AuthDataStore for session tracking
    val authDataStore = remember {
        try {
            (context as? ComponentActivity)?.let { activity ->
                EntryPointAccessors.fromActivity(
                    activity,
                    BaseScreenEntryPoint::class.java
                ).authDataStore()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not access AuthDataStore", e)
            null
        }
    }
    
    // ✅ NEW: Update last activity when screen is accessed and ensure tokens are initialized
    LaunchedEffect(Unit) {
        authDataStore?.let { store ->
            store.updateLastActivity()
            val currentToken = store.getApplicationToken()
            if (currentToken == null || currentToken.isBlank()) {
                Log.w(TAG, "⛔ No valid application token found - skipping keep-alive startup")
                sessionKeepAliveManager?.showSnackbar("No active session. Please log in.")
                return@LaunchedEffect
            }
            if (currentToken == null) {
                Log.w(TAG, "⚠️ No application token found in cache - forcing initialization")
                store.initializeApplicationToken()
                val tokenAfterInit = store.getApplicationToken()
                if (tokenAfterInit == null) {
                    Log.e(TAG, "💀 Still no application token after initialization - session likely expired")
                } else {
                    Log.d(TAG, "✅ Application token restored from storage: "+tokenAfterInit.take(10)+"...")
                }
            }
        }
        if (enableSessionKeepAlive) {
            val token = authDataStore?.getApplicationToken()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "⛔ No valid token - not starting keep-alive")
                sessionKeepAliveManager?.showSnackbar("No active session. Please log in.")
                return@LaunchedEffect
            }
            sessionKeepAliveManager?.let { manager ->
                Log.d(TAG, "🚀 Auto-starting SessionKeepAlive on BaseScreen load")
                manager.startKeepAlive()
                // 🔒 CRITICAL FIX: Solo hacer emergency session check si el token está cerca de expirar
                CoroutineScope(Dispatchers.IO).launch {
                    if (manager.shouldRenewToken()) {
                        Log.d(TAG, "🔄 Token is near expiration, performing emergency session check...")
                    val sessionValid = manager.performEmergencySessionCheck()
                    if (sessionValid) {
                        Log.d(TAG, "✅ Session health check passed - session is valid")
                    } else {
                        Log.w(TAG, "⚠️ Session health check failed - session may be expired")
                        // The TokenErrorHandler will handle the redirect if needed
                        }
                    } else {
                        Log.d(TAG, "✅ Token is still valid, skipping emergency session check on BaseScreen load")
                    }
                }
            }
        }
    }

    // ✅ NEW: Lifecycle-aware SessionKeepAlive management
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, enableSessionKeepAlive) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "🔄 App resumed - managing session keep-alive")
                    onAppResume?.invoke()
                    
                    // ✅ Enhanced session management on app resume
                    if (enableSessionKeepAlive) {
                        val token = authDataStore?.getApplicationToken()
                        if (token.isNullOrBlank()) {
                            Log.w(TAG, "⛔ No valid token on resume - not starting keep-alive")
                            sessionKeepAliveManager?.showSnackbar("No active session. Please log in.")
                            return@LifecycleEventObserver
                        }
                        sessionKeepAliveManager?.let { manager ->
                            Log.d(TAG, "🚀 Starting SessionKeepAlive on app resume")
                            CoroutineScope(Dispatchers.Main).launch {
                                // First check if we're coming back from deep background
                                val sessionValid = manager.onAppComesToForeground()
                                if (sessionValid) {
                                    // CRITICAL: Perform emergency session check if we were in background for a while
                                    val timeSinceLastKeepAlive = manager.getTimeSinceLastKeepAlive()
                                    if (timeSinceLastKeepAlive != null && timeSinceLastKeepAlive > 300_000L) { // 5 minutes
                                        Log.w(TAG, "🚨 App was in background for ${timeSinceLastKeepAlive / 1000}s - performing emergency session check")
                                        val emergencyCheckPassed = manager.performEmergencySessionCheck()
                                        if (!emergencyCheckPassed) {
                                            Log.e(TAG, "💀 Emergency session check failed - session appears expired")
                                            // The TokenErrorHandler will handle the redirect to login
                                            return@launch
                                        }
                                    }
                                    
                                    manager.startKeepAlive()
                                    // Trigger immediate token renewal check for critical operations
                                    manager.triggerTokenRenewalCheck()
                                } else {
                                    Log.w(TAG, "⚠️ Session validation failed on foreground - attempting emergency validation")
                                    // ✅ IMPROVED: Don't give up immediately, try emergency validation
                                    val emergencyCheckPassed = manager.performEmergencySessionCheck()
                                    if (emergencyCheckPassed) {
                                        Log.i(TAG, "✅ Emergency session check passed - session is actually valid")
                                        manager.startKeepAlive()
                                        manager.triggerTokenRenewalCheck()
                                    } else {
                                        Log.e(TAG, "💀 Emergency session check failed - session is truly expired")
                                        // The TokenErrorHandler will handle the redirect to login
                                    }
                                }
                            }
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "⏸️ App paused - transitioning to background mode")
                    
                    // ✅ Transition to background mode (don't stop completely)
                    if (enableSessionKeepAlive) {
                        sessionKeepAliveManager?.let { manager ->
                            Log.d(TAG, "🌙 Transitioning SessionKeepAlive to background mode")
                            manager.onAppGoesToBackground()
                            // Keep running in background with longer intervals
                        }
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.d(TAG, "🛑 App stopped - maintaining background session")
                    // Keep session alive in background for seamless return
                }
                else -> { /* Other lifecycle events */ }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d(TAG, "🧹 BaseScreen disposed")
        }
    }

    // ✅ ENHANCED: Observe authentication state for global session expiration
    val authState by tokenErrorHandler.authenticationState.collectAsState()
    // Eliminar toda la lógica relacionada con showSessionExpiringModal, sessionExpiringSeconds, y el AlertDialog del modal de expiración.
    // El resto de la lógica de keepalive y redirección automática al login se mantiene igual.
    LaunchedEffect(authState) {
        Log.d(TAG, "[DEBUG] LaunchedEffect(authState) triggered. Current state: $authState")
        val currentAuthState = authState
        when (currentAuthState) {
            is AuthenticationState.RequiresAuthentication -> {
                Log.w(TAG, "[DEBUG] Authentication required: ${currentAuthState.message}")
                // ✅ Stop session keep-alive when authentication is required
                if (enableSessionKeepAlive) {
                    sessionKeepAliveManager?.let { manager ->
                        Log.d(TAG, "[DEBUG] Stopping SessionKeepAlive due to auth requirement")
                        manager.stopKeepAlive()
                    }
                }
                Log.d(TAG, "[DEBUG] Navigating to login screen...")
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                Log.d(TAG, "[DEBUG] Navigation to login triggered.")
            }
            is AuthenticationState.Authenticated -> {
                Log.d(TAG, "[DEBUG] Authentication confirmed. User is authenticated.")
                // ✅ Restart session keep-alive when authentication is restored
                if (enableSessionKeepAlive) {
                    sessionKeepAliveManager?.let { manager ->
                        Log.d(TAG, "[DEBUG] Restarting SessionKeepAlive after authentication")
                        manager.startKeepAlive()
                    }
                }
            }
            else -> {
                Log.d(TAG, "[DEBUG] Authentication state is unknown or not handled: $authState")
            }
        }
    }

    // ✅ NEW: Monitor session health and provide user feedback
    if (enableSessionKeepAlive && sessionKeepAliveManager != null) {
        val isKeepAliveActive by sessionKeepAliveManager.isKeepAliveActive.collectAsState()
        val isInBackground by sessionKeepAliveManager.isInBackground.collectAsState()
        
        // Log session status for debugging
        LaunchedEffect(isKeepAliveActive, isInBackground) {
            Log.d(TAG, "📊 Session Status - KeepAlive: $isKeepAliveActive, Background: $isInBackground")
        }
        
        // ✅ Optional: Show session status in debug builds
        // if (BuildConfig.DEBUG && !isKeepAliveActive) {
        //     // Could show a subtle indicator that session management is inactive
        // }
    }

    val sessionExpired = sessionKeepAliveManager?.sessionExpiredEvent?.collectAsStateWithLifecycle()?.value ?: false
    LaunchedEffect(sessionExpired) {
        Log.d(TAG, "[SESSION_FLOW] LaunchedEffect(sessionExpired) triggered. sessionExpired=$sessionExpired")
    if (sessionExpired) {
            Log.e(TAG, "[SESSION_FLOW] 🚨 Session expired event detected. Redirecting to login. StackTrace:")
            Log.e(TAG, Log.getStackTraceString(Throwable()))
            val token = authDataStore?.getApplicationToken()
            val expiration = authDataStore?.getTokenExpirationDate()
            Log.e(TAG, "[SESSION_FLOW] Token at sessionExpired: ${token?.take(30)}... Expiration: $expiration")
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
            Log.d(TAG, "[SESSION_FLOW] Navigation to login triggered by sessionExpiredEvent.")
        }
    }
    if (sessionExpired) {
        // (Mantener el bloque para compatibilidad, pero la navegación ahora está en LaunchedEffect)
    }

    // Mostrar tiempo restante de expiración del token en la UI y en logs
    var tokenTimeLeft by remember { mutableStateOf<Long?>(null) }
    if (enableSessionKeepAlive && sessionKeepAliveManager != null) {
        val tokenExpiration by sessionKeepAliveManager.tokenExpiration.collectAsState(null)
        LaunchedEffect(tokenExpiration) {
            tokenExpiration?.let {
                val now = System.currentTimeMillis()
                val millisLeft = it.time - now
                tokenTimeLeft = if (millisLeft > 0) millisLeft else 0L
                val min = (tokenTimeLeft ?: 0L) / 60000
                val sec = ((tokenTimeLeft ?: 0L) % 60000) / 1000
                Log.d("TokenExpirationInfo", "Tiempo restante de token: ${min}m ${sec}s")
            }
        }
    }

    // Mostrar Snackbar para eventos de sesión/token
    if (enableSessionKeepAlive && sessionKeepAliveManager != null) {
        val snackbarMessage by sessionKeepAliveManager.snackbarEvent.collectAsState()
        var showSnackbar by remember { mutableStateOf(false) }
        LaunchedEffect(snackbarMessage) {
            if (!snackbarMessage.isNullOrBlank()) {
                showSnackbar = true
                delay(3500)
                showSnackbar = false
                sessionKeepAliveManager.clearSnackbarEvent() // Limpiar mensaje de forma segura
            }
        }
        if (showSnackbar && !snackbarMessage.isNullOrBlank()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp)) {
                Snackbar(
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(snackbarMessage!!)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = app.forku.R.color.background_gray))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mostrar tiempo restante de token si está disponible
            if (enableSessionKeepAlive && (tokenTimeLeft != null)) {
                val min = (tokenTimeLeft ?: 0L) / 60000
                val sec = ((tokenTimeLeft ?: 0L) % 60000) / 1000
                Text(
                    text = "Token expires in: ${min}m ${sec}s",
                    color = Color.Gray,
                    modifier = Modifier.padding(8.dp)
                )
            }
            // Network Status Bar at the very top
            NetworkStatusBar(networkManager = networkManager)
            
            Scaffold(
                modifier = modifier.weight(1f),
                topBar = {
                    if (showTopBar) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Back button and actions row
                            if (showBackButton || topBarActions != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 46.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (showBackButton) {
                                            Row(
                                                modifier = Modifier
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = { 
                                                            // ✅ NEW: Trigger keep-alive on user interaction
                                                            if (enableSessionKeepAlive) {
                                                                sessionKeepAliveManager?.triggerKeepAlive()
                                                            }
                                                            navController.navigateUp() 
                                                        }
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowBack,
                                                    contentDescription = "Back",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    "Back",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                        
                                        if (topBarActions != null) {
                                            Box(
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            ) {
                                                topBarActions()
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Title with spacing
                            if (topBarTitle?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(if (showBackButton) 8.dp else 24.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = topBarTitle,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    if (showBottomBar) {
                        ForkUBottomBar(
                            navController = navController,
                            currentVehicleId = currentVehicleId,
                            currentCheckId = currentCheckId,
                            dashboardState = dashboardState ?: DashboardState()
                        )
                    }
                }
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    }
}