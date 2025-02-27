package app.forku.presentation.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()
    
    // Handle navigation
    LaunchedEffect(navigateToDashboard) {
        if (navigateToDashboard) {
            onNavigateToDashboard()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // TopBar with back button
        TopAppBar(
            title = { Text("Vehicle Check") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        when {
            state.isLoading -> LoadingOverlay()
            state.error != null -> ErrorScreen(
                message = "Failed to load checklist: ${state.error}",
                onRetry = { viewModel.loadVehicleAndChecklist() }
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    state.vehicle?.let { vehicle ->
                        VehicleProfileSummary(
                            vehicle = vehicle,
                            status = state.vehicleStatus,
                            showOperatorDetails = false,
                            showFullDetails = false
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Group items by category
                    state.checkItems
                        .groupBy { it.category }
                        .forEach { (category, items) ->
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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.checkItems.all { it.userAnswer != null }) {
                        Button(
                            onClick = { viewModel.submitChecklist() },
                            enabled = !state.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA726),
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Submit Check")
                        }
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
