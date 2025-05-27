package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.common.components.FormFieldDivider
import app.forku.presentation.incident.IncidentReportState


@Composable
fun RootCauseAnalysisSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    // Don't show for Hazard type
    if (state.type == IncidentTypeEnum.HAZARD) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        // Immediate Cause Dropdown
        ImmediateCauseDropdown(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        FormFieldDivider()

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