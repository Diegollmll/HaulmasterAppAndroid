package app.forku.presentation.incident.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.NearMissType
import app.forku.presentation.common.components.CustomOutlinedTextField
import app.forku.presentation.common.components.FormFieldDivider


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
                .padding(vertical = 0.dp)
        )

        FormFieldDivider()

        CommonCausesDropdown(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
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

        FormFieldDivider()

        HazardConsequencesSection(
            state = state,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 8.dp)
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
        VehicleFailTypeDropdown(
            selected = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailFields)?.failureType,
            onSelected = { selectedType ->
                val currentFields = (state.typeSpecificFields as? IncidentTypeFields.VehicleFailFields)
                    ?: IncidentTypeFields.VehicleFailFields(
                        failureType = selectedType,
                        systemAffected = "",
                        maintenanceHistory = "",
                        operationalImpact = "",
                        immediateCause = null,
                        contributingFactors = emptySet(),
                        immediateActions = emptySet(),
                        longTermSolutions = emptySet(),
                        damageOccurrence = emptySet(),
                        environmentalImpact = emptyList(),
                        isLoadCarried = state.isLoadCarried,
                        loadBeingCarried = state.loadBeingCarried,
                        loadWeightEnum = state.loadWeightEnum
                    )

                onValueChange(state.copy(
                    typeSpecificFields = currentFields.copy(failureType = selectedType)
                ))
            },
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
        CustomOutlinedTextField(
            value = selected?.toFriendlyString() ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Near Miss Type",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NearMissType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.toFriendlyString()) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
