package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.CommonCause
import app.forku.domain.model.incident.IncidentTypeFields

@Composable
fun CommonCausesDropdown(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentCause = when (val fields = state.typeSpecificFields) {
        is IncidentTypeFields.CollisionFields -> fields.commonCause?.name
        else -> null
    }?.replace("_", " ")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentCause ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Common Cause") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CommonCause.values().forEach { cause ->
                DropdownMenuItem(
                    text = { Text(cause.name.replace("_", " ")) },
                    onClick = {
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields ->
                                fields.copy(commonCause = cause)
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                        expanded = false
                    }
                )
            }
        }
    }
} 