package app.forku.presentation.vehicle.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-Shift Check", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingOverlay()
            } else if (state.error != null) {
                ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadVehicleAndChecklist() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.checkItems) { item ->
                            ChecklistQuestionItem(
                                question = item,
                                onResponseChanged = viewModel::updateItemResponse
                            )
                        }
                    }

//                    Button(
//                        onClick = { viewModel.submitCheck() },
//                        enabled = state.checkItems.all { it.response != null } && !state.isSubmitting,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 16.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFFFA726),
//                            disabledContainerColor = Color.Gray
//                        )
//                    ) {
//                        if (state.isSubmitting) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(24.dp),
//                                color = Color.Black
//                            )
//                        } else {
//                            Text("Submit Check", color = Color.Black)
//                        }
//                    }

                    Button(
                        onClick = { viewModel.submitCheck() },
                        enabled = state.checkItems.isNotEmpty() && !state.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
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