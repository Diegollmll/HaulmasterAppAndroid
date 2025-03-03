package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.utils.getProposedSolutionsByType


@Composable
fun RootCauseAnalysisSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    // Don't show for Hazard type
    if (state.type == IncidentType.HAZARD) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Root Cause Analysis",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Immediate Cause Dropdown
        ImmediateCauseDropdown(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Contributing Factors Checkboxes
        ContributingFactorsCheckboxes(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

    }
} 