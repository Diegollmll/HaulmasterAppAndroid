package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.CollisionImmediateAction
import app.forku.domain.model.incident.NearMissImmediateAction
import app.forku.domain.model.incident.VehicleFailImmediateAction
import app.forku.domain.model.incident.CollisionLongTermSolution
import app.forku.domain.model.incident.NearMissLongTermSolution
import app.forku.domain.model.incident.VehicleFailLongTermSolution
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import app.forku.presentation.common.components.FormFieldDivider


@Composable
fun PotentialSolutionsSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.type == IncidentTypeEnum.HAZARD) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Immediate Actions
        Text(
            text = "Immediate Actions Taken",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val immediateActions = when (state.type) {
            IncidentTypeEnum.COLLISION -> CollisionImmediateAction.values()
            IncidentTypeEnum.NEAR_MISS -> NearMissImmediateAction.values()
            IncidentTypeEnum.VEHICLE_FAIL -> VehicleFailImmediateAction.values()
            else -> emptyArray()
        }

        immediateActions.forEach { action ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.CollisionFields ->
                    action in fields.immediateActions
                is IncidentTypeFields.NearMissFields ->
                    action in fields.immediateActions
                is IncidentTypeFields.VehicleFailFields ->
                    action in fields.immediateActions
                else -> false
            }

            CheckboxRow(
                text = action.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                checked = isChecked,
                onCheckedChange = { checked ->
                    val newFields = when (val fields = state.typeSpecificFields) {
                        is IncidentTypeFields.CollisionFields -> {
                            val newActions = if (checked) {
                                fields.immediateActions + (action as CollisionImmediateAction)
                            } else {
                                fields.immediateActions - (action as CollisionImmediateAction)
                            }
                            fields.copy(immediateActions = newActions)
                        }
                        is IncidentTypeFields.NearMissFields -> {
                            val newActions = if (checked) {
                                fields.immediateActions + (action as NearMissImmediateAction)
                            } else {
                                fields.immediateActions - (action as NearMissImmediateAction)
                            }
                            fields.copy(immediateActions = newActions)
                        }
                        is IncidentTypeFields.VehicleFailFields -> {
                            val newActions = if (checked) {
                                fields.immediateActions + (action as VehicleFailImmediateAction)
                            } else {
                                fields.immediateActions - (action as VehicleFailImmediateAction)
                            }
                            fields.copy(immediateActions = newActions)
                        }
                        else -> fields
                    }
                    onValueChange(state.copy(typeSpecificFields = newFields))
                }
            )
        }

        FormFieldDivider()

        // Long-term Solutions
        Text(
            text = "Proposed Long-term Solutions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val longTermSolutions = when (state.type) {
            IncidentTypeEnum.COLLISION -> CollisionLongTermSolution.values()
            IncidentTypeEnum.NEAR_MISS -> NearMissLongTermSolution.values()
            IncidentTypeEnum.VEHICLE_FAIL -> VehicleFailLongTermSolution.values()
            else -> emptyArray()
        }

        longTermSolutions.forEach { solution ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.CollisionFields ->
                    solution in fields.longTermSolutions
                is IncidentTypeFields.NearMissFields ->
                    solution in fields.longTermSolutions
                is IncidentTypeFields.VehicleFailFields ->
                    solution in fields.longTermSolutions
                else -> false
            }

            CheckboxRow(
                text = solution.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                checked = isChecked,
                onCheckedChange = { checked ->
                    val newFields = when (val fields = state.typeSpecificFields) {
                        is IncidentTypeFields.CollisionFields -> {
                            val newSolutions = if (checked) {
                                fields.longTermSolutions + (solution as CollisionLongTermSolution)
                            } else {
                                fields.longTermSolutions - (solution as CollisionLongTermSolution)
                            }
                            fields.copy(longTermSolutions = newSolutions)
                        }
                        is IncidentTypeFields.NearMissFields -> {
                            val newSolutions = if (checked) {
                                fields.longTermSolutions + (solution as NearMissLongTermSolution)
                            } else {
                                fields.longTermSolutions - (solution as NearMissLongTermSolution)
                            }
                            fields.copy(longTermSolutions = newSolutions)
                        }
                        is IncidentTypeFields.VehicleFailFields -> {
                            val newSolutions = if (checked) {
                                fields.longTermSolutions + (solution as VehicleFailLongTermSolution)
                            } else {
                                fields.longTermSolutions - (solution as VehicleFailLongTermSolution)
                            }
                            fields.copy(longTermSolutions = newSolutions)
                        }
                        else -> fields
                    }
                    onValueChange(state.copy(typeSpecificFields = newFields))
                }
            )
        }
    }
}

@Composable
private fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
} 