package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.CommonCause
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.presentation.common.components.CustomOutlinedTextField

@Composable
fun CommonCausesDropdown(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentCause = when (val fields = state.typeSpecificFields) {
        is IncidentTypeFields.CollisionFields -> fields.commonCause?.toFriendlyString()
        else -> null
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = currentCause ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Common Cause",
            modifier = Modifier.menuAnchor().fillMaxWidth().padding(vertical = 0.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CommonCause.values().forEach { cause ->
                DropdownMenuItem(
                    text = { Text(cause.toFriendlyString()) },
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