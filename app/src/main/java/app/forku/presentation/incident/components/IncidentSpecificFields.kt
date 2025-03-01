package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.NearMissType
import app.forku.presentation.incident.IncidentReportState
import app.forku.presentation.incident.components.CollisionSpecificFields
import app.forku.presentation.incident.components.NearMissSpecificFields
import app.forku.presentation.incident.components.HazardSpecificFields
import app.forku.presentation.incident.components.VehicleFailureSpecificFields
import app.forku.presentation.incident.model.IncidentTypeFields


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
fun NearMissTypeDropdown(
    selected: NearMissType?,
    onSelected: (NearMissType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.name?.replace("_", " ") ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Near Miss Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NearMissType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.replace("_", " ")) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun NearMissSpecificFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Near Miss Type Dropdown
        NearMissTypeDropdown(
            selected = (state.typeSpecificFields as? IncidentTypeFields.NearMissFields)?.nearMissType,
            onSelected = { selectedType ->
                val currentFields = (state.typeSpecificFields as? IncidentTypeFields.NearMissFields)
                    ?: IncidentTypeFields.NearMissFields()
                onValueChange(state.copy(
                    typeSpecificFields = currentFields.copy(nearMissType = selectedType)
                ))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Activity at Time
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