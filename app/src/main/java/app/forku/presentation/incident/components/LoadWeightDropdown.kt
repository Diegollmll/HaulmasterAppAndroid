package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.LoadWeight

@Composable
fun LoadWeightDropdown(
    selected: LoadWeight?,
    onSelected: (LoadWeight) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.name?.replace("_", " ")?.replace("T", "t") ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Load Weight") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LoadWeight.values().forEach { weight ->
                DropdownMenuItem(
                    text = { Text(weight.name.replace("_", " ").replace("T", "t")) },
                    onClick = {
                        onSelected(weight)
                        expanded = false
                    }
                )
            }
        }
    }
} 