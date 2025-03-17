package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.HazardType
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.presentation.common.components.CustomOutlinedTextField

@Composable
fun HazardTypeDropdown(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentType = when (val fields = state.typeSpecificFields) {
        is IncidentTypeFields.HazardFields -> fields.hazardType?.name
        else -> null
    }?.replace("_", " ")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.HazardFields -> fields.hazardType?.toFriendlyString()
                else -> null
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Hazard Type",
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HazardType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.toFriendlyString()) },
                    onClick = {
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.HazardFields ->
                                fields.copy(hazardType = type)
                            else -> IncidentTypeFields.HazardFields(hazardType = type)
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                        expanded = false
                    }
                )
            }
        }
    }
} 