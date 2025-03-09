package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.InjurySeverity
import app.forku.domain.model.incident.InjuryLocation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.presentation.common.components.CustomOutlinedTextField


@Composable
fun InjurySeverityDropdown(
    selected: InjurySeverity,
    onSelected: (InjurySeverity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = selected.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = "Injuries Reported",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            InjurySeverity.values().forEach { severity ->
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

@Composable
fun InjuryLocationsDropdown(
    selected: Set<InjuryLocation>,
    onSelectionChanged: (Set<InjuryLocation>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = if (selected.isEmpty()) "" else selected.joinToString(", ") { it.name.replace("_", " ") },
            onValueChange = {},
            readOnly = true,
            label = "Injury Locations",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            InjuryLocation.values().forEach { location ->
                DropdownMenuItem(
                    text = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = location in selected,
                                onCheckedChange = null
                            )
                            Text(
                                text = location.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    onClick = {
                        val newSelection = if (location in selected) {
                            selected - location
                        } else {
                            selected + location
                        }
                        onSelectionChanged(newSelection)
                    }
                )
            }
        }
    }
} 