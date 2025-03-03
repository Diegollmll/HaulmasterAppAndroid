package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.HazardCorrectiveAction
import app.forku.domain.model.incident.IncidentTypeFields

@Composable
fun HazardImmediateActionsSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.type != IncidentType.HAZARD) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Corrective Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        HazardCorrectiveAction.values().forEach { action ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.HazardFields ->
                    action in fields.correctiveActions
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
                                val newActions = if (checked) {
                                    fields.correctiveActions + action
                                } else {
                                    fields.correctiveActions - action
                                }
                                fields.copy(correctiveActions = newActions)
                            }
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                    }
                )
                
                Text(
                    text = action.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 