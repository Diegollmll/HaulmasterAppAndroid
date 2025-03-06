package app.forku.presentation.dashboard.components

import androidx.compose.foundation.layout.*
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
import app.forku.presentation.common.components.UserDateTimer
import app.forku.presentation.common.components.OverlappingImages
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.remember

@Composable
fun SessionCard(
    vehicle: Vehicle? = null,
    status: VehicleStatus = VehicleStatus.AVAILABLE,
    lastCheck: PreShiftCheck? = null,
    user: User? = null,
    isActive: Boolean = false,
    sessionStartTime: String? = null
) {
    val startDateTime = remember(sessionStartTime) {
        sessionStartTime?.let {
            try {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                null
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){
            Text(
                text = if (isActive) "Active session," else "Welcome ${user?.name}!,",
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(0.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {


                // User info section
                user?.let {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (isActive && vehicle != null) {
                            OverlappingImages(
                                mainImageUrl = vehicle.photoUrl,
                                overlayImageUrl = user.photoUrl,
                                mainTint = MaterialTheme.colorScheme.onSurface,
                                mainSize = 48,
                                overlaySize = 24
                            )
                        }
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
                    UserDateTimer(
                        modifier = Modifier.fillMaxWidth(),
                        sessionStartTime = if (isActive) startDateTime else null
                    )
                } else {
                    Text(
                        text = "Last check: ${lastCheck?.lastCheckDateTime} - ${lastCheck?.status}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        
    }
} 