package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState

@Composable
fun CollisionPeopleFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.injuries,
            onValueChange = { onValueChange(state.copy(injuries = it)) },
            label = { Text("Injuries Sustained") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        OutlinedTextField(
            value = state.othersInvolved.joinToString("\n"),
            onValueChange = { 
                onValueChange(state.copy(
                    othersInvolved = it.split("\n").filter { line -> line.isNotBlank() }
                ))
            },
            label = { Text("Other People Involved") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun NearMissPeopleFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicPeopleFields(state, onValueChange, modifier)
}

@Composable
fun BasicPeopleFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // Reporter field (read-only)
        OutlinedTextField(
            value = state.operatorId ?: "Unknown",
            onValueChange = { },
            label = { Text("Reported By") },
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.othersInvolved.joinToString("\n"),
            onValueChange = { 
                onValueChange(state.copy(
                    othersInvolved = it.split("\n").filter { line -> line.isNotBlank() }
                ))
            },
            label = { Text("People Present") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
} 