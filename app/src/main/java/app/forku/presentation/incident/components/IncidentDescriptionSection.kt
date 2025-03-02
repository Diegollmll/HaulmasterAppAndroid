package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import getImmediateActionsByType

@Composable
fun IncidentDescriptionSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Incident Description",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Activity at Time
        OutlinedTextField(
            value = state.activityAtTime,
            onValueChange = { onValueChange(state.copy(activityAtTime = it)) },
            label = { Text("Activity at Time") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { onValueChange(state.copy(description = it)) },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            minLines = 4
        )

        // Immediate Actions
        Text(
            text = "Immediate Actions Taken",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        getImmediateActionsByType(state.type).forEach { action ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = action in state.immediateActions,
                    onCheckedChange = { checked ->
                        val newActions = if (checked) {
                            state.immediateActions + action
                        } else {
                            state.immediateActions - action
                        }
                        onValueChange(state.copy(immediateActions = newActions))
                    }
                )
                Text(action)
            }
        }
    }
} 