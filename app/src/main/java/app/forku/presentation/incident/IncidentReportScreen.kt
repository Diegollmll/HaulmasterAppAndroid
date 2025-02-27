package app.forku.presentation.incident

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.incident.IncidentType

@Composable
fun IncidentReportScreen(
    viewModel: IncidentReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Report Incident",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Incident Type Selection
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Incident Type",
                style = MaterialTheme.typography.titleMedium
            )
            IncidentType.values().forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.type == type,
                            onClick = { viewModel.setType(type) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.type == type,
                        onClick = { viewModel.setType(type) }
                    )
                    Text(
                        text = type.name.replace("_", " "),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Description Field
        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.setDescription(it) },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            minLines = 3
        )

        // Submit Button
        Button(
            onClick = { viewModel.submitReport() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Submit Report")
            }
        }

        // Error Message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    // Success Dialog
    if (state.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissSuccessDialog()
                viewModel.resetForm()
                onNavigateBack()
            },
            title = { Text("Success") },
            text = { Text("Incident report submitted successfully") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissSuccessDialog()
                    viewModel.resetForm()
                    onNavigateBack()
                }) {
                    Text("OK")
                }
            }
        )
    }
} 