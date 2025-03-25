package app.forku.presentation.checklist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.vehicle.profile.components.VehicleProfileSummary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.AppModal
import app.forku.presentation.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = hiltViewModel(),
    navController: NavController,
    onBackPressed: () -> Unit,
    networkManager: NetworkConnectivityManager
) {
    var showConfirmationDialog = remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    
    // Handle back button press
    BackHandler {
        // Check if we should navigate to a specific dashboard
        when (state?.message) {
            "admin_dashboard" -> navController.navigate(Screen.AdminDashboard.route) {
                popUpTo(Screen.AdminDashboard.route) { inclusive = true }
            }
            "dashboard" -> navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
            else -> onBackPressed()
        }
    }
    
    if (showConfirmationDialog.value) {
        AppModal(
            onDismiss = { showConfirmationDialog.value = false },
            onConfirm = {
                showConfirmationDialog.value = false
                viewModel.submitCheck()
            },
            title = "Submit Checklist",
            message = "Are you sure you want to submit this checklist?"
        )
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        viewModel = viewModel,
        topBarTitle = "Pre-Shift Check",
        networkManager = networkManager,
        onRefresh = { viewModel.loadChecklistData() },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (val currentState = state) {
                    null -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        if (currentState.isLoading) {
                            LoadingOverlay()
                        }
                        
                        if (currentState.error != null) {
                            ErrorScreen(
                                message = currentState.error,
                                onRetry = { viewModel.loadChecklistData() }
                            )
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Keep VehicleProfileSummary
                            currentState.vehicle?.let { vehicle ->
                                VehicleProfileSummary(
                                    vehicle = vehicle,
                                    status = vehicle.status
                                )
                            }

                            // Add padding to the questionnaire section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Group items by category
                                val groupedItems = currentState.checkItems?.groupBy { it.category }
                                groupedItems?.forEach { (category, items) ->
                                    // Keep CategoryHeader
                                    CategoryHeader(
                                        categoryName = category,
                                        modifier = Modifier.padding(bottom = 1.dp)
                                    )

                                    items.forEach { item ->
                                        ChecklistQuestionItem(
                                            question = item,
                                            onResponseChanged = viewModel::updateItemResponse,
                                            modifier = Modifier.padding(bottom = 1.dp)
                                        )
                                    }
                                }


                                // Only show submit button when all items are answered
                                if (currentState.showSubmitButton && currentState.allAnswered) {
                                    Button(
                                        onClick = { showConfirmationDialog.value = true },
                                        enabled = currentState.showSubmitButton && currentState.allAnswered,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Submit Checklist")
                                    }
                                }
                            }

                            currentState.message?.let { message ->
                                Snackbar(
                                    modifier = Modifier
                                        .padding(16.dp)
                                ) {
                                    Text(message)
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    // Handle navigation from viewModel
    LaunchedEffect(viewModel.navigateBack.collectAsState().value) {
        if (viewModel.navigateBack.value) {
            // Check state message for navigation route
            when (state?.message) {
                "admin_dashboard" -> navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                }
                "dashboard" -> navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }
                else -> onBackPressed()
            }
            viewModel.resetNavigation()
        }
    }
}

@Composable
fun CategoryHeader(
    categoryName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = categoryName.replace("_", " ").capitalize(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
    }
}