package app.forku.presentation.vehicle.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.vehicle.Vehicle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.vehicle.components.VehicleProfileSummary
import app.forku.presentation.vehicle.components.VehicleQrCodeModal
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.presentation.checklist.ChecklistQuestionItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.hilt.navigation.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    viewModel: VehicleProfileViewModel,
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onPreShiftCheck: (String) -> Unit,
    onScanQrCode: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.vehicle != null && !state.hasActiveSession && !state.hasActivePreShiftCheck) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color(0xFFFFA726)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Show QR Code") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.toggleQrCode()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Show QR"
                                        )
                                    }
                                )
                                
                                DropdownMenuItem(
                                    text = { Text("Pre-Shift Check") },
                                    onClick = {
                                        showMenu = false
                                        onPreShiftCheck(state.vehicle?.id ?: "")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Pre-Shift Check"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> LoadingOverlay()
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadVehicle() }
                )
                state.vehicle != null -> {
                    val vehicle = checkNotNull(state.vehicle)
                    Box(modifier = Modifier.fillMaxSize()) {
                        VehicleProfileContent(
                            vehicle = vehicle,
                            onShowQrCode = { viewModel.toggleQrCode() }
                        )

                        if (state.showQrCode) {
                            VehicleQrCodeModal(
                                vehicleId = vehicle.id,
                                onDismiss = viewModel::toggleQrCode
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleProfileContent(
    vehicle: Vehicle,
    onShowQrCode: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            CurrentVehicleProfileSummary(vehicle = vehicle)

            Spacer(modifier = Modifier.height(32.dp))

            VehicleDetailsSection(vehicle)

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CurrentVehicleProfileSummary(vehicle: Vehicle) {
    VehicleProfileSummary(vehicle = vehicle)
}

@Composable
fun VehicleDetailsSection(vehicle: Vehicle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Best Suited for",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = vehicle.bestSuitedFor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = vehicle.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ChecklistSection(
    checkItems: List<ChecklistItem>,
    isSubmitting: Boolean,
    onUpdateResponse: (String, Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Pre-Shift Check",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val unansweredItems = checkItems.filter { it.userAnswer == null }
        
        if (unansweredItems.isEmpty() && !isSubmitting) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726)
                )
            ) {
                Text("Submit Check")
            }
        } else {
            unansweredItems.forEach { item ->
                AnimatedVisibility(
                    visible = true,
                    exit = fadeOut() + slideOutHorizontally()
                ) {
                    ChecklistQuestionItem(
                        question = item,
                        onResponseChanged = onUpdateResponse,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
