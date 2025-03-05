package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun BaseScreen(
    navController: NavController,
    viewModel: ViewModel? = null,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    topBarTitle: String = "",
    onRefresh: (() -> Unit)? = null,
    showLoadingOnRefresh: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle screen focus for refresh
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

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(text = topBarTitle) },
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Atrás"
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                ForkUBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
} 