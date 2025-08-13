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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues

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
    
    // Eliminar: SessionKeepAliveManager y toda lógica relacionada
    // - No obtenerlo por Hilt
    // - No usarlo en ciclo de vida ni autenticación
    // - No observar su estado ni eventos
    // - No mostrar snackbar ni manejar expiración de sesión por él
    // Mantener solo AuthDataStore, TokenErrorHandler y navegación

    // ✅ ENHANCED: Observe authentication state for global session expiration
    val authState by tokenErrorHandler.authenticationState.collectAsState()
    LaunchedEffect(authState) {
        Log.d(TAG, "[DEBUG] LaunchedEffect(authState) triggered. Current state: $authState")
        val currentAuthState = authState
        when (currentAuthState) {
            is AuthenticationState.RequiresAuthentication -> {
                Log.w(TAG, "[DEBUG] Authentication required: ${currentAuthState.message}")
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
                Log.d(TAG, "[DEBUG] Navigation to login triggered.")
            }
            is AuthenticationState.Authenticated -> {
                Log.d(TAG, "[DEBUG] Authentication confirmed. User is authenticated.")
            }
            else -> {
                Log.d(TAG, "[DEBUG] Authentication state is unknown or not handled: $authState")
            }
        }
    }

    // Mostrar tiempo restante de expiración del token en la UI y en logs
    var tokenTimeLeft by remember { mutableStateOf<Long?>(null) }
    val authDataStore = (LocalContext.current as? ComponentActivity)?.let { activity ->
        EntryPointAccessors.fromActivity(
            activity,
            BaseScreenEntryPoint::class.java
        ).authDataStore()
    }
    val expiration = authDataStore?.getTokenExpirationDate()
    LaunchedEffect(expiration) {
        expiration?.let {
                val now = System.currentTimeMillis()
                val millisLeft = it.time - now
                tokenTimeLeft = if (millisLeft > 0) millisLeft else 0L
                val min = (tokenTimeLeft ?: 0L) / 60000
                val sec = ((tokenTimeLeft ?: 0L) % 60000) / 1000
                Log.d("TokenExpirationInfo", "Tiempo restante de token: ${min}m ${sec}s")
            }
    }

    // Eliminar bloque de snackbarEvent innecesario para sesión/token

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = app.forku.R.color.background_gray))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mostrar tiempo restante de token si está disponible
            // if (enableSessionKeepAlive && (tokenTimeLeft != null)) {
            //     val min = (tokenTimeLeft ?: 0L) / 60000
            //     val sec = ((tokenTimeLeft ?: 0L) % 60000) / 1000
            //     val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            //     Text(
            //         text = "Token expires in: ${min}m ${sec}s",
            //         color = Color.Gray,
            //         modifier = Modifier
            //             .padding(
            //                 top = statusBarPadding + 8.dp, // 8.dp extra para separación visual
            //                 start = 8.dp,
            //                 end = 8.dp,
            //                 bottom = 8.dp
            //             )
            //     )
            // }
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