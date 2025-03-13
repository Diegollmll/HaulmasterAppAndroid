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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import app.forku.presentation.common.components.CustomOutlinedTextField
import app.forku.presentation.common.components.FormFieldDivider
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

    Column(modifier = modifier.fillMaxWidth()) {
        
        // Location Details field
        CustomOutlinedTextField(
            value = state.locationDetails,
            onValueChange = { onValueChange(state.copy(locationDetails = it)) },
            label = "Location Details",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
            minLines = 1
        )

        FormFieldDivider()

        CustomOutlinedTextField(
            value = buildString {
                append(dateFormatter.format(Date(state.date)))
                state.incidentTime?.let { time ->
                    append(" at ")
                    append(timeFormatter.format(Date(state.date)))
                }
            },
            onValueChange = { },
            label = "Date and Time",
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, "Select date and time")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 3.dp)
        )

        FormFieldDivider()

        // Hidden Weather Conditions field - maintains state but not visible
        if (true) {  // This ensures the composable is never rendered
            CustomOutlinedTextField(
                value = state.weather,
                onValueChange = { },
                label = "Weather Conditions",
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp)
            )
            FormFieldDivider()
        }

        // Hidden Location field - maintains state but not visible
        if (true) {  // This ensures the composable is never rendered
            CustomOutlinedTextField(
                value = state.location,
                onValueChange = { },
                label = "Location",
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp)
            )
            FormFieldDivider()
        }

        // Add Severity Level Dropdown
        SeverityLevelDropdown(
            selected = state.severityLevel,
            onSelected = { severity ->
                onValueChange(state.copy(severityLevel = severity))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
        )

        FormFieldDivider()

        // Type Specific Section with dynamic title
        IncidentSpecificTypeFieldsSelector(state = state, onValueChange = onValueChange)

        //Spacer(modifier = Modifier.height(16.dp))
    }
}
