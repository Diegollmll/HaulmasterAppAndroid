package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.CollisionContributingFactor
import app.forku.domain.model.incident.NearMissContributingFactor
import app.forku.domain.model.incident.VehicleFailContributingFactor

@Composable
fun ContributingFactorsCheckboxes(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Contributing Factors",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val factors = when (state.type) {
            IncidentType.COLLISION -> CollisionContributingFactor.values()
            IncidentType.NEAR_MISS -> NearMissContributingFactor.values()
            IncidentType.VEHICLE_FAIL -> VehicleFailContributingFactor.values()
            else -> emptyArray()
        }

        factors.forEach { factor ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.CollisionFields ->
                    factor in fields.contributingFactors
                is IncidentTypeFields.NearMissFields ->
                    factor in fields.contributingFactors
                is IncidentTypeFields.VehicleFailFields ->
                    factor in fields.contributingFactors
                else -> false
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        val newFields = when (val fields = state.typeSpecificFields) {
                            is IncidentTypeFields.CollisionFields -> {
                                val newFactors = if (checked) {
                                    fields.contributingFactors + (factor as CollisionContributingFactor)
                                } else {
                                    fields.contributingFactors - (factor as CollisionContributingFactor)
                                }
                                fields.copy(contributingFactors = newFactors)
                            }
                            is IncidentTypeFields.NearMissFields -> {
                                val newFactors = if (checked) {
                                    fields.contributingFactors + (factor as NearMissContributingFactor)
                                } else {
                                    fields.contributingFactors - (factor as NearMissContributingFactor)
                                }
                                fields.copy(contributingFactors = newFactors)
                            }
                            is IncidentTypeFields.VehicleFailFields -> {
                                val newFactors = if (checked) {
                                    fields.contributingFactors + (factor as VehicleFailContributingFactor)
                                } else {
                                    fields.contributingFactors - (factor as VehicleFailContributingFactor)
                                }
                                fields.copy(contributingFactors = newFactors)
                            }
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                    }
                )
                Text(
                    text = factor.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 