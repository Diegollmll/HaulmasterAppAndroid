package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.CollisionImmediateCause
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.NearMissImmediateCause
import app.forku.domain.model.incident.VehicleFailImmediateCause
import app.forku.presentation.common.components.CustomOutlinedTextField

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
        is IncidentTypeFields.VehicleFailFields -> fields.immediateCause?.name
        else -> null
    }?.replace("_", " ")

    val causes = when (state.type) {
        IncidentTypeEnum.COLLISION -> CollisionImmediateCause.values()
        IncidentTypeEnum.NEAR_MISS -> NearMissImmediateCause.values()
        IncidentTypeEnum.VEHICLE_FAIL -> VehicleFailImmediateCause.values()
        else -> emptyArray()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.CollisionFields -> fields.immediateCause?.let { cause ->
                    when (cause) {
                        CollisionImmediateCause.OPERATOR_ERROR -> "Operator Error"
                        CollisionImmediateCause.MECHANICAL_FAILURE -> "Mechanical Failure"
                        CollisionImmediateCause.OTHER -> "Other Cause"
                    }
                }
                is IncidentTypeFields.NearMissFields -> fields.immediateCause?.let { cause ->
                    when (cause) {
                        NearMissImmediateCause.OPERATOR_ERROR -> "Operator Error"
                        NearMissImmediateCause.ENVIRONMENTAL_FACTOR -> "Environmental Factor"
                        NearMissImmediateCause.EQUIPMENT_ISSUE -> "Equipment Issue"
                        NearMissImmediateCause.OTHER -> "Other Cause"
                    }
                }
                is IncidentTypeFields.VehicleFailFields -> fields.immediateCause?.let { cause ->
                    when (cause) {
                        VehicleFailImmediateCause.WEAR_AND_TEAR -> "Wear and Tear"
                        VehicleFailImmediateCause.LACK_OF_MAINTENANCE -> "Lack of Maintenance"
                        VehicleFailImmediateCause.OPERATOR_MISUSE -> "Operator Misuse"
                        VehicleFailImmediateCause.ENVIRONMENTAL_FACTORS -> "Environmental Factors"
                        VehicleFailImmediateCause.OTHER -> "Other Cause"
                    }
                }
                else -> null
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = "Immediate Cause",
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            causes.forEach { cause ->
                DropdownMenuItem(
                    text = { 
                        Text(when (cause) {
                            is CollisionImmediateCause -> when (cause) {
                                CollisionImmediateCause.OPERATOR_ERROR -> "Operator Error"
                                CollisionImmediateCause.MECHANICAL_FAILURE -> "Mechanical Failure"
                                CollisionImmediateCause.OTHER -> "Other Cause"
                            }
                            is NearMissImmediateCause -> when (cause) {
                                NearMissImmediateCause.OPERATOR_ERROR -> "Operator Error"
                                NearMissImmediateCause.ENVIRONMENTAL_FACTOR -> "Environmental Factor"
                                NearMissImmediateCause.EQUIPMENT_ISSUE -> "Equipment Issue"
                                NearMissImmediateCause.OTHER -> "Other Cause"
                            }
                            is VehicleFailImmediateCause -> when (cause) {
                                VehicleFailImmediateCause.WEAR_AND_TEAR -> "Wear and Tear"
                                VehicleFailImmediateCause.LACK_OF_MAINTENANCE -> "Lack of Maintenance"
                                VehicleFailImmediateCause.OPERATOR_MISUSE -> "Operator Misuse"
                                VehicleFailImmediateCause.ENVIRONMENTAL_FACTORS -> "Environmental Factors"
                                VehicleFailImmediateCause.OTHER -> "Other Cause"
                            }
                            else -> cause.toString()
                        })
                    },
                    onClick = {
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields ->
                                fields.copy(immediateCause = cause as CollisionImmediateCause)
                            is IncidentTypeFields.NearMissFields ->
                                fields.copy(immediateCause = cause as NearMissImmediateCause)
                            is IncidentTypeFields.VehicleFailFields ->
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