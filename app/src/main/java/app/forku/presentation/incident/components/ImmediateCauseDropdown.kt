package app.forku.presentation.incident.components

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
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.CollisionImmediateCause
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.NearMissImmediateCause
import app.forku.domain.model.incident.VehicleFailImmediateCause

@Composable
fun ImmediateCauseDropdown(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val currentCause = when (val fields = state.typeSpecificFields) {
        is IncidentTypeFields.CollisionFields -> fields.immediateCause?.name
        is IncidentTypeFields.NearMissFields -> fields.immediateCause?.name
        is IncidentTypeFields.VehicleFailureFields -> fields.immediateCause?.name
        else -> null
    }?.replace("_", " ")

    val causes = when (state.type) {
        IncidentType.COLLISION -> CollisionImmediateCause.values()
        IncidentType.NEAR_MISS -> NearMissImmediateCause.values()
        IncidentType.VEHICLE_FAIL -> VehicleFailImmediateCause.values()
        else -> emptyArray()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentCause ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Immediate Cause") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            causes.forEach { cause ->
                DropdownMenuItem(
                    text = { Text(cause.name.replace("_", " ")) },
                    onClick = {
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields ->
                                fields.copy(immediateCause = cause as CollisionImmediateCause)
                            is IncidentTypeFields.NearMissFields ->
                                fields.copy(immediateCause = cause as NearMissImmediateCause)
                            is IncidentTypeFields.VehicleFailureFields ->
                                fields.copy(immediateCause = cause as VehicleFailImmediateCause)
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