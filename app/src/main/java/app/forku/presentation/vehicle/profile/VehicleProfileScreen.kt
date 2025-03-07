package app.forku.presentation.vehicle.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.forku.domain.model.vehicle.Vehicle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.vehicle.components.VehicleQrCodeModal
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.presentation.vehicle.profile.components.VehicleProfileSummary
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    viewModel: VehicleProfileViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onPreShiftCheck: (String) -> Unit,
    onScanQrCode: () -> Unit,
    navController: NavController
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.checkId) {
        state.checkId?.let { checkId ->
            navController.navigate(
                "checklist/${state.vehicle?.id}?checkId=${checkId}"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadVehicle()
    }

    LaunchedEffect(state.hasActivePreShiftCheck) {
        android.util.Log.d("appflow", "VehicleProfileScreen LaunchedEffect state.hasActivePreShiftCheck: ${state.hasActivePreShiftCheck}")
        android.util.Log.d("appflow", "VehicleProfileScreen LaunchedEffect state.hasActiveSession: ${state.hasActiveSession}")
        android.util.Log.d("appflow", "VehicleProfileScreen LaunchedEffect state.vehicle?.status?.name: ${state.vehicle?.status?.name}")
//        if (!state.hasActivePreShiftCheck && !state.hasActiveSession) {
//            //viewModel.startSessionFromCheck()
////            val lastCheck = checklistRepository.getLastPreShiftCheck(state.vehicle?.id ?: return@LaunchedEffect)
////            if (lastCheck?.status == PreShiftStatus.COMPLETED_PASS.toString()) {
////            }
//        }
    }

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

                    if (state.vehicle != null && state.vehicle?.status != VehicleStatus.IN_USE && !state.hasActiveSession) {
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
                                    text = { Text( if (state.hasActivePreShiftCheck) "Continue Pre-Shift Check"
                                    else "Start Pre-Shift Check") },
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
                            state = state,
                            modifier = Modifier.fillMaxSize()
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
fun VehicleProfileContent(
    state: VehicleProfileState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            state.vehicle?.let { vehicle ->
                val status = vehicle.status

                VehicleProfileSummary(
                    vehicle = vehicle,
                    status = status,
                    activeOperator = state.activeOperator,
                    showOperatorDetails = true,
                    showPreShiftCheckDetails = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                VehicleDetailsSection(vehicle = vehicle)
            }
        }
    }
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

