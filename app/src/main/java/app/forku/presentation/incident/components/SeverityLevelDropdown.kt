package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.IncidentSeverityLevel
import app.forku.presentation.common.components.CustomOutlinedTextField

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
        CustomOutlinedTextField(
            value = selected?.name?.replace("_", " ") ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Severity Level",
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
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