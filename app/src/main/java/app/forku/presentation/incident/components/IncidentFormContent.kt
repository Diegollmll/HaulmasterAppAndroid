package app.forku.presentation.incident.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.common.components.ExpandableCard

@Composable
fun IncidentFormContent(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Basic Incident Details Section (Always visible)
        ExpandableCard(
            title = "Incident Details",
            initiallyExpanded = true,
            style = MaterialTheme.typography.titleMedium
        ) {
            IncidentDetailsSection(state = state, onValueChange = onValueChange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.type == IncidentType.HAZARD) {

            ExpandableCard(
                title = "Immediate Actions Taken",
                style = MaterialTheme.typography.titleMedium
            ) {
                HazardImmediateActionsSection(state = state, onValueChange = onValueChange)
            }


        }


        if (state.type in listOf(IncidentType.COLLISION, IncidentType.NEAR_MISS)) {
            Spacer(modifier = Modifier.height(8.dp))

            // People Involved Section
            ExpandableCard(
                title = "People Involved",
                style = MaterialTheme.typography.titleMedium
            ) {
                PeopleInvolvedSection(state = state, onValueChange = onValueChange)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vehicle Info Section (Only for specific types)
        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            ExpandableCard(
                title = "Vehicle Info",
                style = MaterialTheme.typography.titleMedium
            ) {
                VehicleInfoSection(state = state, onValueChange = onValueChange)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Incident Description Section with Photos
        ExpandableCard(
            title = "Incident Description",
            style = MaterialTheme.typography.titleMedium
        ) {
            IncidentDescriptionSection(
                state = state,
                onValueChange = onValueChange,
                onAddPhoto = onAddPhoto
            )
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            Spacer(modifier = Modifier.height(8.dp))

            ExpandableCard(
                title = "Root Cause Analysis",
                style = MaterialTheme.typography.titleMedium
            ) {
                RootCauseAnalysisSection(state = state, onValueChange = onValueChange)
            }
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL)) {
            Spacer(modifier = Modifier.height(8.dp))

            ExpandableCard(
                title = "Damage & Impact",
                style = MaterialTheme.typography.titleMedium
            ) {
                DamageAndImpactSection(state = state, onValueChange = onValueChange)
            }
        }

        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            Spacer(modifier = Modifier.height(8.dp))

            ExpandableCard(
                title = "Potential Solutions",
                style = MaterialTheme.typography.titleMedium
            ) {
                PotentialSolutionsSection(
                    state = state,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Add Hazard Preventive Measures section only for HAZARD type
        if (state.type == IncidentType.HAZARD) {
            Spacer(modifier = Modifier.height(8.dp))

            ExpandableCard(
                title = "Preventive Measures",
                style = MaterialTheme.typography.titleMedium
            ) {
                HazardPreventiveMeasuresSection(state = state, onValueChange = onValueChange)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = { /* TODO: Handle submit */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Submit")
        }
    }
} 