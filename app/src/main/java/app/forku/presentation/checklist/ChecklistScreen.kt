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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val navigateBack by viewModel.navigateBack.collectAsState()

    LaunchedEffect(navigateBack) {
        if (navigateBack) {
            onNavigateBack()
            viewModel.resetNavigation()
        }
    }

    BackHandler {
        viewModel.onBackPressed()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-shift Check") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

            if (state.isLoading) {
                LoadingOverlay()
            }

            state.error?.let { error ->
                ErrorScreen(
                    message = error,
                    onRetry = { viewModel.loadVehicleAndChecklist() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Keep VehicleProfileSummary
                state.vehicle?.let { vehicle ->
                    VehicleProfileSummary(
                        vehicle = vehicle,
                        status = state.vehicleStatus
                    )
                }

                // Add padding to the questionnaire section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Group items by category
                    val groupedItems = state.checkItems.groupBy { it.category }
                    groupedItems.forEach { (category, items) ->
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
                    if (!state.isLoading &&
                        state.checkItems.isNotEmpty() &&
                        state.checkItems.all { it.userAnswer != null }) {
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

                state.message?.let { message ->
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