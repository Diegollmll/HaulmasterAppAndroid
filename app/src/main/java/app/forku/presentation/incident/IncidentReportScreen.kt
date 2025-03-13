package app.forku.presentation.incident


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.presentation.incident.components.IncidentTopBar
import app.forku.presentation.incident.components.IncidentFormContent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.setValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.model.incident.toDisplayText
import android.Manifest
import android.content.pm.PackageManager
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.ForkuButton
import app.forku.presentation.common.components.LocationPermissionHandler


@Composable
fun IncidentReportScreen(
    incidentType: String,
    onNavigateBack: () -> Unit,
    viewModel: IncidentReportViewModel = hiltViewModel(),
    navController: NavController,
    networkManager: NetworkConnectivityManager
) {
    val state by viewModel.state.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val navigateToDashboard by viewModel.navigateToDashboard.collectAsState()
    val context = LocalContext.current
    
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(navigateToDashboard) {
        if (navigateToDashboard) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
            viewModel.resetNavigation()
        }
    }

    LaunchedEffect(incidentType) {
        viewModel.setIncidentType(incidentType)
    }
    
    // Launcher for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.tempPhotoUri?.let { viewModel.addPhoto(it) }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhoto(it) }
    }

    // Photo source selection dialog
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Add Photo") },
            text = { Text("Choose photo source") },
            confirmButton = {
                ForkuButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                ForkuButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        viewModel.createTempPhotoUri(context)?.let { uri ->
                            cameraLauncher.launch(uri)
                        }
                    }
                ) {
                    Text("Camera")
                }
            }
        )
    }

    LocationPermissionHandler(
        locationSettingsException = locationState.locationSettingsException,
        onPermissionsGranted = viewModel::onLocationPermissionGranted,
        onPermissionsDenied = viewModel::onLocationPermissionDenied,
        onLocationSettingsDenied = viewModel::onLocationSettingsDenied
    )

    BaseScreen(
        navController = navController,
        viewModel = viewModel,
        showBottomBar = false,
        topBarTitle = "${IncidentType.valueOf(incidentType).toDisplayText()} Incident",
        networkManager = networkManager
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            IncidentFormContent(
                state = state,
                onValueChange = { viewModel.updateState(it) },
                onAddPhoto = { showPhotoSourceDialog = true },
                modifier = Modifier.weight(1f)
            )

            SubmitButton(
                onClick = { viewModel.submitIncident() },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }

    // Success Dialog
    if (state.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = { Text("Success") },
            text = { Text("Incident report submitted successfully") },
            confirmButton = {
                ForkuButton(
                    onClick = { viewModel.dismissSuccessDialog() }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Error Dialog
    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                ForkuButton(
                    onClick = { viewModel.clearError() }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SubmitButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var lastClickTime by remember { mutableStateOf(0L) }
    
    ForkuButton(
        onClick = {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 1000) { // 1 second debounce
                lastClickTime = currentTime
                onClick()
            }
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Text("Submit Report")
    }
}

