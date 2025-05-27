package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.HazardPreventiveMeasure
import app.forku.domain.model.incident.IncidentTypeFields

@Composable
fun HazardPreventiveMeasuresSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.type != IncidentTypeEnum.HAZARD) return

    Column(modifier = modifier.fillMaxWidth()) {

        Text(
            text = "Recommended Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        HazardPreventiveMeasure.values().forEach { measure ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.HazardFields ->
                    measure in fields.preventiveMeasures
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
                            is IncidentTypeFields.HazardFields -> {
                                val newMeasures = if (checked) {
                                    fields.preventiveMeasures + measure
                                } else {
                                    fields.preventiveMeasures - measure
                                }
                                fields.copy(preventiveMeasures = newMeasures)
                            }
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                    }
                )
                
                Text(
                    text = measure.toFriendlyString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 