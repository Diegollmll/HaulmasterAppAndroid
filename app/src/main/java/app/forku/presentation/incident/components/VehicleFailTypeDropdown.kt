package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.VehicleFailType
import app.forku.presentation.common.components.CustomOutlinedTextField

@Composable
fun VehicleFailTypeDropdown(
    selected: VehicleFailType?,
    onSelected: (VehicleFailType) -> Unit,
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
            label = "Type of Failure",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            VehicleFailType.values().forEach { type ->
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