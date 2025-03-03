package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.HazardType
import app.forku.domain.model.incident.IncidentTypeFields

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
        OutlinedTextField(
            value = currentType ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Hazard Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HazardType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.replace("_", " ")) },
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