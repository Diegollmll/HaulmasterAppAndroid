package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.IncidentTypeEnum
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
        
        // Add fields based on incident type
        when (state.type) {
            IncidentTypeEnum.COLLISION -> CollisionPeopleFields(state, onValueChange)
            IncidentTypeEnum.NEAR_MISS -> NearMissPeopleFields(state, onValueChange)
            else -> BasicPeopleFields(state, onValueChange)
        }
    }
}
