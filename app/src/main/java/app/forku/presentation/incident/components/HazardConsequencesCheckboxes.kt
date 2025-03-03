package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.HazardConsequence
import app.forku.domain.model.incident.IncidentTypeFields

@Composable
fun HazardConsequencesSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Potential Consequences",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        HazardConsequence.values().forEach { consequence ->
            val isChecked = when (val fields = state.typeSpecificFields) {
                is IncidentTypeFields.HazardFields ->
                    consequence in fields.potentialConsequences
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
                                val newConsequences = if (checked) {
                                    fields.potentialConsequences + consequence
                                } else {
                                    fields.potentialConsequences - consequence
                                }
                                fields.copy(potentialConsequences = newConsequences)
                            }
                            else -> fields
                        }
                        onValueChange(state.copy(typeSpecificFields = newFields))
                    }
                )
                
                Text(
                    text = consequence.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 