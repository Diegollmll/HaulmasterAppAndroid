package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState

@Composable
fun WeatherConditionsField(
    state: IncidentReportState,
    weather: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = weather,
        onValueChange = { },
        label = { Text("Weather Conditions") },
        readOnly = true,
        enabled = false,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
} 