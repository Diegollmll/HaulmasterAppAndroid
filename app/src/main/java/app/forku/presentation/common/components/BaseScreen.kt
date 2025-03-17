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
    onRefresh: (() -> Unit)? = null,
    showLoadingOnRefresh: Boolean = false,
    networkManager: NetworkConnectivityManager,
    content: @Composable (PaddingValues) -> Unit
) {
    // Lifecycle observer for refresh
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefresh?.invoke()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            // Back button with additional top padding
                            if (showBackButton) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 46.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = { navController.navigateUp() }
                                            )
                                            .padding(8.dp),
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