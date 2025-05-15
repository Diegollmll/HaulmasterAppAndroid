package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.fillMaxWidth
import app.forku.domain.model.vehicle.Vehicle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.forku.presentation.common.components.CustomOutlinedTextField

@Composable
fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicleId: String?,
    onVehicleSelected: (Vehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedVehicle = remember(selectedVehicleId, vehicles) {
        vehicles.find { it.id == selectedVehicleId }
    }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        CustomOutlinedTextField(
            value = selectedVehicle?.let { "${it.codename} (${it.type.Name})" } ?: "Select Vehicle",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            vehicles.forEach { vehicle ->
                DropdownMenuItem(
                    text = { Text("${vehicle.codename} (${vehicle.type.Name})") },
                    onClick = {
                        onVehicleSelected(vehicle)
                        expanded = false
                    }
                )
            }
        }
    }
} 