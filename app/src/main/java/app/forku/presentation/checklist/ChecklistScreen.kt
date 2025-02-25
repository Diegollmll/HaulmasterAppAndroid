package app.forku.presentation.checklist

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.vehicle.components.VehicleProfileSummary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()

    LaunchedEffect(navigateToDashboard) {
        if (navigateToDashboard) {
            onComplete()
            viewModel.resetNavigation()
        }
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted && state.isSubmitted) {
            onComplete()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Pre-Shift Check", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
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
                            VehicleProfileSummary(vehicle = vehicle)
                            Spacer(modifier = Modifier.height(24.dp))
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
                                onClick = { viewModel.submitCheck() },
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

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text("Retry")
        }
    }
}