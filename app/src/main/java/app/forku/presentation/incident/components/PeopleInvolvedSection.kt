package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState

@Composable
fun PeopleInvolvedSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "People Involved",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Add fields based on incident type
        when (state.type) {
            IncidentType.COLLISION -> CollisionPeopleFields(state, onValueChange)
            IncidentType.NEAR_MISS -> NearMissPeopleFields(state, onValueChange)
            else -> BasicPeopleFields(state, onValueChange)
        }
    }
}
