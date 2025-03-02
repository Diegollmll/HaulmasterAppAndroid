package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import java.time.LocalTime
import android.app.TimePickerDialog
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IncidentDetailsSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Date formatter
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Time picker dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            onValueChange(state.copy(
                date = calendar.timeInMillis,
                incidentTime = LocalTime.of(hour, minute)
            ))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // Date picker dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onValueChange(state.copy(date = calendar.timeInMillis))
            // Show time picker after date is selected
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // DateTime field
        OutlinedTextField(
            value = buildString {
                append(dateFormatter.format(Date(state.date)))
                state.incidentTime?.let { time ->
                    append(" at ")
                    append(timeFormatter.format(Date(state.date)))
                }
            },
            onValueChange = { },
            label = { Text("Date and Time") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, "Select date and time")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { datePickerDialog.show() }
        )

        OutlinedTextField(
            value = state.location,
            onValueChange = { onValueChange(state.copy(location = it)) },
            label = { Text("Location") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            enabled = false,
            placeholder = { Text("Waiting for location...") }
        )
        
        OutlinedTextField(
            value = state.weather,
            onValueChange = { onValueChange(state.copy(weather = it)) },
            label = { Text("Weather Conditions") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}
