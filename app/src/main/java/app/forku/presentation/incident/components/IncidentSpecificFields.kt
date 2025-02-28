package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.components.CollisionSpecificFields
import app.forku.presentation.incident.components.NearMissSpecificFields
import app.forku.presentation.incident.components.HazardSpecificFields
import app.forku.presentation.incident.components.VehicleFailureSpecificFields

@Composable
fun CollisionSpecificFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.activityAtTime,
            onValueChange = { onValueChange(state.copy(activityAtTime = it)) },
            label = { Text("Activity at Time of Collision") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun NearMissSpecificFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.activityAtTime,
            onValueChange = { onValueChange(state.copy(activityAtTime = it)) },
            label = { Text("Activity at Time of Near Miss") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun HazardSpecificFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.activityAtTime,
            onValueChange = { onValueChange(state.copy(activityAtTime = it)) },
            label = { Text("Activity When Hazard Identified") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun VehicleFailureSpecificFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.activityAtTime,
            onValueChange = { onValueChange(state.copy(activityAtTime = it)) },
            label = { Text("Activity When Failure Occurred") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
} 