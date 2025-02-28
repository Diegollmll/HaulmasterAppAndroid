package app.forku.presentation.incident

import LocationPermissionHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.model.IncidentFormSection
import app.forku.presentation.incident.components.BasicInfoSection
import app.forku.presentation.incident.components.CollisionSpecificFields
import app.forku.presentation.incident.components.PeopleInvolvedSection
import app.forku.presentation.incident.components.VehicleInfoSection
import app.forku.presentation.incident.components.IncidentDetailsSection
import app.forku.presentation.incident.components.RootCauseAnalysisSection
import app.forku.presentation.incident.components.DocumentationSection
import app.forku.presentation.incident.components.HazardSpecificFields
import app.forku.presentation.incident.components.NearMissSpecificFields
import app.forku.presentation.incident.components.VehicleFailureSpecificFields
import android.app.Activity
import android.content.IntentSender
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.setValue


@Composable
fun IncidentReportScreen(
    incidentType: String,
    onNavigateBack: () -> Unit,
    viewModel: IncidentReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Handle location settings resolution
    LaunchedEffect(state.locationSettingsException) {
        state.locationSettingsException?.let { exception ->
            activity?.let { nonNullActivity ->
                try {
                    // Show location settings dialog
                    exception.startResolutionForResult(nonNullActivity, LOCATION_SETTINGS_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                    android.util.Log.e("IncidentScreen", "Error showing location settings dialog", sendEx)
                }
            }
        }
    }

    LocationPermissionHandler(
        onPermissionsGranted = {
            viewModel.onLocationPermissionGranted()
        },
        onPermissionsDenied = {
            viewModel.onLocationPermissionDenied()
        }
    )

    LaunchedEffect(incidentType) {
        viewModel.setIncidentType(incidentType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Report Incident")
                        state.type?.let { incidentType ->
                            Text(
                                text = incidentType.toString().replace("_", " ").capitalize(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Basic Info Section - Always visible
            ExpandableCard(
                title = "Basic Information",
                initiallyExpanded = true
            ) {
                BasicInfoSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // People Involved Section
            ExpandableCard(title = "People Involved") {
                PeopleInvolvedSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle Info - Show only for relevant incident types
            if (state.type in listOf(
                IncidentType.COLLISION,
                IncidentType.VEHICLE_FAIL,
                IncidentType.NEAR_MISS
            )) {
                ExpandableCard(title = "Vehicle Information") {
                    VehicleInfoSection(
                        state = state,
                        onValueChange = { viewModel.updateState(it) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Incident Type Specific Fields
            when (state.type) {
                IncidentType.COLLISION -> {
                    ExpandableCard(title = "Collision Details") {
                        CollisionSpecificFields(
                            state = state,
                            onValueChange = { viewModel.updateState(it) }
                        )
                    }
                }
                IncidentType.HAZARD -> {
                    ExpandableCard(title = "Hazard Details") {
                        HazardSpecificFields(
                            state = state,
                            onValueChange = { viewModel.updateState(it) }
                        )
                    }
                }
                IncidentType.NEAR_MISS -> {
                    ExpandableCard(title = "Near Miss Details") {
                        NearMissSpecificFields(
                            state = state,
                            onValueChange = { viewModel.updateState(it) }
                        )
                    }
                }
                IncidentType.VEHICLE_FAIL -> {
                    ExpandableCard(title = "Vehicle Failure Details") {
                        VehicleFailureSpecificFields(
                            state = state,
                            onValueChange = { viewModel.updateState(it) }
                        )
                    }
                }
                else -> null
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Documentation Section - Always visible
            ExpandableCard(title = "Documentation") {
                DocumentationSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) },
                    onAddPhoto = { /* TODO: Implement photo selection */ }
                )
            }

            Button(
                onClick = { viewModel.submitReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Submit Report")
            }
        }
    }

    // Show success dialog
    if (state.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = { Text("Success") },
            text = { Text("Incident report submitted successfully") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissSuccessDialog()
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ExpandableCard(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expanded) 
                        Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            if (expanded) {
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

private const val LOCATION_SETTINGS_REQUEST = 1001