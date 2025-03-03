package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.IncidentSeverityLevel

@Composable
fun SeverityLevelDropdown(
    selected: IncidentSeverityLevel?,
    onSelected: (IncidentSeverityLevel) -> Unit,
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
            label = { Text("Incident Severity Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            IncidentSeverityLevel.values().forEach { severity ->
                DropdownMenuItem(
                    text = { Text(severity.name.replace("_", " ")) },
                    onClick = {
                        onSelected(severity)
                        expanded = false
                    }
                )
            }
        }
    }
} 