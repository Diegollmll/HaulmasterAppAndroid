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
import app.forku.presentation.common.theme.BackgroundGray
import app.forku.presentation.dashboard.DashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = false,
    showBottomBar: Boolean = false,
    currentVehicleId: String? = null,
    currentCheckId: String? = null,
    dashboardState: DashboardState? = null,
    viewModel: ViewModel? = null,
    topBarTitle: String = "",
    onRefresh: (() -> Unit)? = null,
    showLoadingOnRefresh: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val backgroundColor = BackgroundGray

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh silencioso al volver a la pantalla
                onRefresh?.invoke()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header section
            if (showTopBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Back button
                    TextButton(
                        onClick = { navController.navigateUp() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Back",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    // Title with spacing
                    if (topBarTitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
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

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                content(PaddingValues(horizontal = 2.dp))
            }

            // Bottom Bar
            if (showBottomBar) {
                ForkUBottomBar(
                    navController = navController,
                    currentVehicleId = currentVehicleId,
                    currentCheckId = currentCheckId,
                    dashboardState = dashboardState ?: DashboardState()
                )
            }
        }
    }
} 