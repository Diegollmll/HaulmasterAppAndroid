package app.forku.presentation.incident

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

@Composable
fun IncidentReportScreen(
    incidentType: String,
    onNavigateBack: () -> Unit,
    viewModel: IncidentReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(incidentType) {
        viewModel.setIncidentType(incidentType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Incident") },
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
                .padding(16.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = when (state.currentSection) {
                    is IncidentFormSection.BasicInfo -> 0.2f
                    is IncidentFormSection.PeopleInvolved -> 0.4f
                    is IncidentFormSection.VehicleInfo -> 0.6f
                    is IncidentFormSection.IncidentDetails -> 0.8f
                    else -> 1f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // Current section
            when (state.currentSection) {
                is IncidentFormSection.BasicInfo -> BasicInfoSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
                is IncidentFormSection.PeopleInvolved -> PeopleInvolvedSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
                is IncidentFormSection.VehicleInfo -> VehicleInfoSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
                is IncidentFormSection.IncidentDetails -> IncidentDetailsSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
                is IncidentFormSection.RootCauseAnalysis -> RootCauseAnalysisSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) }
                )
                is IncidentFormSection.Documentation -> DocumentationSection(
                    state = state,
                    onValueChange = { viewModel.updateState(it) },
                    onAddPhoto = { /* TODO: Implement photo selection */ }
                )
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { viewModel.previousSection() },
                    enabled = state.currentSection != IncidentFormSection.BasicInfo
                ) {
                    Text("Previous")
                }
                
                Button(
                    onClick = { 
                        if (state.currentSection == IncidentFormSection.Documentation) {
                            viewModel.submitReport()
                        } else {
                            viewModel.nextSection()
                        }
                    }
                ) {
                    Text(
                        if (state.currentSection == IncidentFormSection.Documentation) 
                            "Submit" else "Next"
                    )
                }
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
fun IncidentReportForm(
    incidentType: IncidentType,
    onSubmit: () -> Unit
) {
    val state = remember { mutableStateOf(IncidentReportState(type = incidentType)) }
    
    Column {
        // Common fields
        BasicInfoSection(
            state = state.value,
            onValueChange = { state.value = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Conditional sections based on type
        when (incidentType) {
            IncidentType.COLLISION -> CollisionSpecificFields(
                state = state.value,
                onValueChange = { state.value = it }
            )
            IncidentType.NEAR_MISS -> NearMissSpecificFields(
                state = state.value,
                onValueChange = { state.value = it }
            )
            IncidentType.HAZARD -> HazardSpecificFields(
                state = state.value,
                onValueChange = { state.value = it }
            )
            IncidentType.VEHICLE_FAIL -> VehicleFailureSpecificFields(
                state = state.value,
                onValueChange = { state.value = it }
            )
        }
    }
} 