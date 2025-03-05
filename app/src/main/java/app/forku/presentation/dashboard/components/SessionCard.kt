package app.forku.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.presentation.vehicle.profile.components.VehicleStatusIndicator
import app.forku.presentation.common.components.DateTime

@Composable
fun SessionCard(
    vehicle: Vehicle? = null,
    status: VehicleStatus = VehicleStatus.AVAILABLE,
    lastCheck: PreShiftCheck? = null,
    user: User? = null,
    isActive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // First Column - User Info Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = if (isActive) "Active session," else "Welcome!,",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // User info section
                user?.let {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = it.role.name ?: "Operator",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Second Column - Vehicle/Clock Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (isActive && vehicle != null) {
                    Text(
                        text = "Vehicle: ${vehicle.codename}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    VehicleStatusIndicator(status = status)
                    DateTime(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {

                }
            }
        }
    }
} 