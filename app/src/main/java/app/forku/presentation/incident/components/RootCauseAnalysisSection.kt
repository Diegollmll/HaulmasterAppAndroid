package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.utils.getProposedSolutionsByType

@Composable
fun RootCauseAnalysisSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Root Cause Analysis",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Proposed Solutions
        Text(
            text = "Proposed Solutions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        getProposedSolutionsByType(state.type).forEach { solution ->
            OutlinedTextField(
                value = solution,
                onValueChange = { /* Read only */ },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        // Custom solutions
        OutlinedTextField(
            value = state.proposedSolutions.joinToString("\n"),
            onValueChange = { 
                onValueChange(state.copy(
                    proposedSolutions = it.split("\n").filter { line -> line.isNotBlank() }
                ))
            },
            label = { Text("Additional Solutions") },
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
} 