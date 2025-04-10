package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.incident.IncidentReportState
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.checklist.getPreShiftStatusColor
import app.forku.domain.model.checklist.getPreShiftStatusText
import app.forku.domain.model.incident.IncidentType
import app.forku.presentation.common.components.CustomOutlinedTextField
import app.forku.presentation.common.components.FormFieldDivider
import app.forku.presentation.common.utils.getRelativeTimeSpanFromDateTime
import app.forku.presentation.common.utils.getRelativeTimeSpanString


@Composable
fun VehicleInfoSection(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Always show vehicle selector, but with selected vehicle if exists
        VehicleSelector(
            vehicles = state.availableVehicles,
            selectedVehicleId = state.vehicleId,
            onVehicleSelected = { vehicle ->
                onValueChange(state.copy(
                    vehicleId = vehicle.id,
                    vehicleType = vehicle.type,
                    vehicleName = vehicle.codename
                ))
                // The ViewModel will handle loading the preshift check
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        FormFieldDivider()

        // Vehicle Type (Auto-filled)
        if(true) {
            CustomOutlinedTextField(
                value = state.vehicleType?.name ?: "",
                onValueChange = { /* Read-only */ },
                label = "Vehicle Type",
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            FormFieldDivider()
        }

        // Vehicle Name (Auto-filled)
        CustomOutlinedTextField(
            value = state.vehicleName,
            onValueChange = { /* Read-only */ },
            label = "Vehicle Name",
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        FormFieldDivider()

        if(true) {
            // Vehicle ID (Auto-filled)
            CustomOutlinedTextField(
                value = state.vehicleId ?: "",
                onValueChange = { /* Read-only */ },
                label = "Vehicle ID",
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            FormFieldDivider()
        }

        // Last Preshift Check Date/Time
        CustomOutlinedTextField(
            value = state.lastPreshiftCheck?.let { getRelativeTimeSpanFromDateTime(it) } ?: "No preshift check recorded",
            onValueChange = { /* Read-only */ },
            label = "Checklist",
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 0.dp)
        )

        // Check ID field
        if (state.checkId != null) {
            FormFieldDivider()
            
            CustomOutlinedTextField(
                value = state.checkId,
                onValueChange = { /* Read-only */ },
                label = "Check ID",
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        // Preshift Check Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getPreShiftStatusText(state.preshiftCheckStatus),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 0.dp, top = 0.dp),
                color = getPreShiftStatusColor(state.preshiftCheckStatus)
            )
        }

        // Load Being Carried Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Was Load Being Carried?",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = state.isLoadCarried,
                onCheckedChange = { checked ->
                    onValueChange(state.copy(isLoadCarried = checked))
                }
            )
        }

        if (state.isLoadCarried) {
            FormFieldDivider()
            
            // Load Being Carried Details
            CustomOutlinedTextField(
                value = state.loadBeingCarried,
                onValueChange = { onValueChange(state.copy(loadBeingCarried = it)) },
                label = "Load Being Carried",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            FormFieldDivider()

            // Load Weight Dropdown
            LoadWeightDropdown(
                selected = state.loadWeight,
                onSelected = { selectedWeight ->
                    onValueChange(state.copy(loadWeight = selectedWeight))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
} 