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
import app.forku.core.network.NetworkConnectivityManager
import androidx.compose.ui.Alignment
import app.forku.presentation.common.components.OptionsDropdownMenu
import app.forku.presentation.common.components.DropdownMenuOption
import app.forku.domain.model.user.UserRole
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    viewModel: VehicleProfileViewModel = hiltViewModel(),
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onPreShiftCheck: (String) -> Unit,
    onScanQrCode: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    userRole: UserRole  // Removed default ADMIN value to force proper injection
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showStatusDialog by remember { mutableStateOf(false) }

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

    // Status selection dialog
    if (showStatusDialog && userRole == UserRole.ADMIN) {  // Only show dialog for admin users
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Vehicle Status") },
            text = {
                Column {
                    VehicleStatus.values().forEach { status ->
                        Button(
                            onClick = {
                                viewModel.updateVehicleStatus(status)
                                showStatusDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(status.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                    if (state.vehicle != null) {
                        val vehicle = state.vehicle // Store vehicle in local variable
                        val isVehicleOutOfService = vehicle?.status == VehicleStatus.OUT_OF_SERVICE
                        val shouldShowMenu = userRole == UserRole.ADMIN || !isVehicleOutOfService

                        if (shouldShowMenu) {
                            val options = buildList {
                                if (userRole == UserRole.ADMIN) {
                                    // Admin-only options
                                    add(DropdownMenuOption(
                                        text = "Show QR Code",
                                        onClick = { viewModel.toggleQrCode() },
                                        leadingIcon = Icons.Default.Info,
                                        enabled = true,
                                        adminOnly = true
                                    ))

                                    if (state.hasActiveSession) {
                                        add(DropdownMenuOption(
                                            text = "End Vehicle Session",
                                            onClick = { viewModel.endVehicleSession() },
                                            leadingIcon = Icons.Default.Stop,
                                            enabled = true,
                                            adminOnly = true,
                                            iconTint = Color.Red
                                        ))
                                    }

                                    add(DropdownMenuOption(
                                        text = "Change Vehicle Status",
                                        onClick = { showStatusDialog = true },
                                        leadingIcon = Icons.Default.Edit,
                                        enabled = true,
                                        adminOnly = true
                                    ))
                                }
                                
                                // Options available to all users
                                add(DropdownMenuOption(
                                    text = if (state.hasActivePreShiftCheck) "Continue Pre-Shift Check" else "Start Pre-Shift Check",
                                    onClick = { onPreShiftCheck(vehicle?.id ?: "") },
                                    leadingIcon = Icons.Default.CheckCircle,
                                    enabled = vehicle?.status == VehicleStatus.AVAILABLE && !state.hasActiveSession
                                ))
                            }

                            OptionsDropdownMenu(
                                options = options,
                                isAdmin = userRole == UserRole.ADMIN,
                                isEnabled = true
                            )
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

                        if (state.showQrCode && userRole == UserRole.ADMIN) {
                            VehicleQrCodeModal(
                                vehicleId = vehicle.id,
                                onDismiss = viewModel::toggleQrCode,
                                onShare = viewModel::shareQrCode
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

