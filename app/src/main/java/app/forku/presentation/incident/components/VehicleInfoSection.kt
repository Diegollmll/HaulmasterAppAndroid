package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.incident.IncidentType

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

        // Vehicle Type (Auto-filled)
        OutlinedTextField(
            value = state.vehicleType?.name?.replace("_", " ") ?: "",
            onValueChange = { /* Read-only */ },
            label = { Text("Vehicle Type") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Vehicle Name (Auto-filled)
        OutlinedTextField(
            value = state.vehicleName,
            onValueChange = { /* Read-only */ },
            label = { Text("Vehicle Name") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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

        // Last Preshift Check Date/Time
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        OutlinedTextField(
            value = state.lastPreshiftCheck?.format(formatter) ?: "No preshift check recorded",
            onValueChange = { /* Read-only */ },
            label = { Text("Last Preshift Check") },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Preshift Check Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Preshift Check Status:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            val statusCondition = state.preshiftCheckStatus == CheckStatus.COMPLETED_PASS.toString()
            val statusColor = if (statusCondition) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
            
            Text(
                text = if (statusCondition) "PASSED" else "FAILED",
                color = statusColor,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (state.type != IncidentType.VEHICLE_FAIL){
            // Load Being Carried
            OutlinedTextField(
                value = state.loadBeingCarried,
                onValueChange = { onValueChange(state.copy(loadBeingCarried = it)) },
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
                    //TODO: consolidate with other already enum
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
} 