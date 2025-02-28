package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.components.BasicInfoSection
import app.forku.presentation.incident.components.DocumentationSection
import app.forku.presentation.incident.components.PeopleInvolvedSection
import app.forku.presentation.incident.components.VehicleInfoSection
import app.forku.presentation.common.components.ExpandableCard

@Composable
fun IncidentFormContent(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Basic Info Section
        ExpandableCard(
            title = "Basic Information",
            initiallyExpanded = true
        ) {
            BasicInfoSection(state = state, onValueChange = onValueChange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // People Involved Section
        ExpandableCard(title = "People Involved") {
            PeopleInvolvedSection(state = state, onValueChange = onValueChange)
        }

        // Vehicle Info Section
        if (state.type in listOf(IncidentType.COLLISION, IncidentType.VEHICLE_FAIL, IncidentType.NEAR_MISS)) {
            Spacer(modifier = Modifier.height(8.dp))
            ExpandableCard(title = "Vehicle Information") {
                VehicleInfoSection(state = state, onValueChange = onValueChange)
            }
        }

        // Type Specific Section
        Spacer(modifier = Modifier.height(8.dp))
        IncidentTypeSpecificSection(state = state, onValueChange = onValueChange)

        // Documentation Section
        Spacer(modifier = Modifier.height(8.dp))
        ExpandableCard(title = "Documentation") {
            DocumentationSection(
                state = state,
                onValueChange = onValueChange,
                onAddPhoto = { /* TODO */ }
            )
        }
    }
} 