package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.CollisionType
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.presentation.common.components.CustomOutlinedTextField


@Composable
fun CollisionTypeDropdown(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentType = when (val fields = state.typeSpecificFields) {
        is IncidentTypeFields.CollisionFields -> fields.collisionType?.name
        else -> null
    }?.replace("_", " ")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = currentType ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Collision Type",
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(vertical = 0.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CollisionType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.replace("_", " ")) },
                    onClick = {
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields ->
                                fields.copy(collisionType = type)
                            else -> IncidentTypeFields.CollisionFields(collisionType = type)
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                        expanded = false
                    }
                )
            }
        }
    }
} 