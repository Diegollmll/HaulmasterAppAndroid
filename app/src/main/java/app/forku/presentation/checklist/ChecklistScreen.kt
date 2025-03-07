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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = hiltViewModel(),
    navController: NavController,
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    BaseScreen(
        navController = navController,
        viewModel = viewModel,
        showBottomBar = false,
        topBarTitle = "Pre-Shift Check"
    ) { padding ->
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
                                    categoryName = category.name,
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
                            if (!currentState.isLoading &&
                                currentState.checkItems.isNotEmpty() &&
                                currentState.checkItems.all { it.userAnswer != null }) {
                                Button(
                                    onClick = { viewModel.submitCheck() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(text = "Submit Check")
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

    // Handle back navigation
    LaunchedEffect(viewModel.navigateBack.collectAsState().value) {
        if (viewModel.navigateBack.value) {
            onBackPressed()
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