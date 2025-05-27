package app.forku.presentation.incident.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.LoadWeightEnum
import app.forku.presentation.common.components.CustomOutlinedTextField

@Composable
fun LoadWeightDropdown(
    selected: LoadWeightEnum?,
    onSelected: (LoadWeightEnum) -> Unit,
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
            label = "Load Weight",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LoadWeightEnum.values().forEach { weight ->
                DropdownMenuItem(
                    text = { Text(weight.toFriendlyString()) },
                    onClick = {
                        onSelected(weight)
                        expanded = false
                    }
                )
            }
        }
    }
} 