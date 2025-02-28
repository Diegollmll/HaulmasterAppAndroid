package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import app.forku.domain.model.incident.IncidentType

@Composable
fun IncidentTopBar(
    incidentType: IncidentType?,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text("Report Incident")
                incidentType?.let { type ->
                    Text(
                        text = type.toString().replace("_", " ").capitalize(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        }
    )
} 