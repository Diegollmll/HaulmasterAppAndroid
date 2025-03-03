package app.forku.presentation.incident.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.NearMissType


@Composable
fun CollisionTypeSpecificField(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CollisionTypeDropdown(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CommonCausesDropdown(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}


@Composable
fun NearMissTypeSpecificField(
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
    }



}

@Composable
fun HazardTypeSpecificField(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HazardTypeDropdown(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        HazardConsequencesSection(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun VehicleFailSpecificField(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        VehicleFailureTypeDropdown(
            selected = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)?.failureType,
            onSelected = { selectedType ->
                val currentFields = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)
                    ?: IncidentTypeFields.VehicleFailureFields()
                onValueChange(state.copy(
                    typeSpecificFields = currentFields.copy(failureType = selectedType)
                ))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Load Being Carried Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Was Load Being Carried?",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)?.isLoadCarried ?: false,
                onCheckedChange = { checked ->
                    val currentFields = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)
                        ?: IncidentTypeFields.VehicleFailureFields()
                    onValueChange(state.copy(
                        typeSpecificFields = currentFields.copy(isLoadCarried = checked)
                    ))
                }
            )
        }

        // Load Weight Dropdown (only visible if load was being carried)
        if ((state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)?.isLoadCarried == true) {
            LoadWeightDropdown(
                selected = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)?.loadWeight,
                onSelected = { selectedWeight ->
                    val currentFields = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailureFields)
                        ?: IncidentTypeFields.VehicleFailureFields()
                    onValueChange(state.copy(
                        typeSpecificFields = currentFields.copy(loadWeight = selectedWeight)
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
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
