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
        
        // Basic Info Section (Always visible)
        ExpandableCard(
            title = "Basic Information",
            initiallyExpanded = true,
            style = MaterialTheme.typography.titleMedium
        ) {
            IncidentDetailsSection(state = state, onValueChange = onValueChange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type Specific Section with dynamic title
        IncidentTypeSpecificSection(state = state, onValueChange = onValueChange)

        Spacer(modifier = Modifier.height(8.dp))

        // Incident Details Section
        ExpandableCard(
            title = "Incident Details",
            style = MaterialTheme.typography.titleMedium
        ) {
            IncidentDescriptionSection(state = state, onValueChange = onValueChange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // People Involved Section
        ExpandableCard(
            title = "People Involved",
            style = MaterialTheme.typography.titleMedium
        ) {
            PeopleInvolvedSection(state = state, onValueChange = onValueChange)
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

        // Documentation Section (Always visible)
        ExpandableCard(
            title = "Documentation",
            style = MaterialTheme.typography.titleMedium
        ) {
            DocumentationSection(
                state = state,
                onValueChange = onValueChange,
                onAddPhoto = onAddPhoto
            )
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