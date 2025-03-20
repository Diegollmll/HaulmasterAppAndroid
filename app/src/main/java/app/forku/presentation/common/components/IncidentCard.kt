package app.forku.presentation.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentStatus

@Composable
fun IncidentCard(
    type: String,
    date: String,
    description: String,
    status: IncidentStatus? = null,
    creatorName: String = "Unknown",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = type,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By $creatorName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                status?.let { incidentStatus ->
                    Text(
                        text = incidentStatus.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = when (incidentStatus) {
                            IncidentStatus.REPORTED -> MaterialTheme.colorScheme.primary
                            IncidentStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.tertiary
                            IncidentStatus.RESOLVED -> MaterialTheme.colorScheme.secondary
                            IncidentStatus.CLOSED -> MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }
        }
    }
} 