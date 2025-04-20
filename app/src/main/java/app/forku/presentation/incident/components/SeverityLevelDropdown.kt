package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.IncidentSeverityLevelEnum
import app.forku.presentation.common.components.CustomOutlinedTextField

@Composable
fun SeverityLevelDropdown(
    selected: IncidentSeverityLevelEnum?,
    onSelected: (IncidentSeverityLevelEnum) -> Unit,
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
            label = "Severity Level",
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            IncidentSeverityLevelEnum.values().forEach { severity ->
                DropdownMenuItem(
                    text = { Text(severity.toFriendlyString()) },
                    onClick = {
                        onSelected(severity)
                        expanded = false
                    }
                )
            }
        }
    }
} 