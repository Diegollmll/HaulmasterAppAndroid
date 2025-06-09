package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.DamageOccurrence
import app.forku.domain.model.incident.EnvironmentalImpact
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.presentation.common.components.FormFieldDivider

@Composable
fun DamageAndImpactSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show for Collision and Vehicle Failure
    if (state.type !in listOf(IncidentTypeEnum.COLLISION, IncidentTypeEnum.VEHICLE_FAIL)) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Damage Occurrence Checkboxes
        Text(
            text = "Damage Occurrence *",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val currentDamages = when (val fields = state.typeSpecificFields) {
            is IncidentTypeFields.CollisionFields -> fields.damageOccurrence
            is IncidentTypeFields.VehicleFailFields -> fields.damageOccurrence
            else -> emptySet()
        }

        DamageOccurrence.values().forEach { damage ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = damage in currentDamages,
                    onCheckedChange = { checked ->
                        val newDamages = if (checked) {
                            currentDamages + damage
                        } else {
                            currentDamages - damage
                        }
                        
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields ->
                                fields.copy(damageOccurrence = newDamages)
                            is IncidentTypeFields.VehicleFailFields ->
                                fields.copy(damageOccurrence = newDamages)
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                    }
                )
                Text(
                    text = damage.toFriendlyString(),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        FormFieldDivider()

        // Environmental Impact Checkboxes
        Text(
            text = "Environmental Impact",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        EnvironmentalImpact.values().forEach { impact ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.CollisionFields ->
                    fields.environmentalImpact?.contains(impact) == true
                is IncidentTypeFields.VehicleFailFields ->
                    fields.environmentalImpact?.contains(impact) == true
                else -> false
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields -> {
                                val currentImpacts = fields.environmentalImpact?.toMutableSet() ?: mutableSetOf()
                                if (checked) {
                                    currentImpacts.add(impact)
                                } else {
                                    currentImpacts.remove(impact)
                                }
                                fields.copy(environmentalImpact = currentImpacts)
                            }
                            is IncidentTypeFields.VehicleFailFields -> {
                                val currentImpacts = fields.environmentalImpact?.toMutableSet() ?: mutableSetOf()
                                if (checked) {
                                    currentImpacts.add(impact)
                                } else {
                                    currentImpacts.remove(impact)
                                }
                                fields.copy(environmentalImpact = currentImpacts)
                            }
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                    }
                )
                Text(
                    text = impact.toFriendlyString(),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 