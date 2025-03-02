package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState

@Composable
fun VehicleInfoSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Vehicle Information",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Vehicle ID (Auto-filled)
        OutlinedTextField(
            value = state.vehicleId ?: "",
            onValueChange = { /* Read-only */ },
            label = { Text("Vehicle ID") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Load Being Carried
        OutlinedTextField(
            value = state.loadType,
            onValueChange = { onValueChange(state.copy(loadType = it)) },
            label = { Text("Load Being Carried") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Load Weight
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = state.loadWeight,
                onValueChange = { },
                readOnly = true,
                label = { Text("Load Weight") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(vertical = 8.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                listOf("<1t", "1-3t", "3-5t", ">5t").forEach { weight ->
                    DropdownMenuItem(
                        text = { Text(weight) },
                        onClick = { 
                            onValueChange(state.copy(loadWeight = weight))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
} 