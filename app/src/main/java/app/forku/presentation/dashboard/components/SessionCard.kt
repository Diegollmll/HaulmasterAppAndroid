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
import app.forku.domain.model.session.VehicleSession
import app.forku.presentation.common.utils.formatDateTime


@Composable
fun SessionCard(
    vehicle: Vehicle?,
    lastCheck: PreShiftCheck?,
    user: User?,
    currentSession: VehicleSession?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        SessionContent(
            vehicle = vehicle,
            status = if (currentSession != null) VehicleStatus.IN_USE else vehicle?.status ?: VehicleStatus.AVAILABLE,
            user = user,
            startDateTime = remember(currentSession?.startTime) {
                currentSession?.startTime?.let {
                    try {
                        // Parseamos la fecha UTC y la convertimos a LocalDateTime
                        val instant = java.time.Instant.parse(it)
                        instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                    } catch (e: Exception) {
                        null
                    }
                }
            },
            lastCheck = lastCheck,
            isActive = currentSession != null
        )
    }
}

@Composable
private fun SessionContent(
    vehicle: Vehicle?,
    status: VehicleStatus,
    user: User?,
    startDateTime: LocalDateTime?,
    lastCheck: PreShiftCheck?,
    isActive: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        
        Text(
            text = if (isActive) "Active Session" else "Welcome ${user?.name ?: ""}!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column - User/Vehicle Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                user?.let {
                    if (vehicle != null) {
                        OverlappingImages(
                            mainImageUrl = vehicle.photoModel,
                            overlayImageUrl = user.photoUrl,
                            mainTint = MaterialTheme.colorScheme.onSurface,
                            mainSize = 90,
                            overlaySize = 45
                        )
                    }
                }
            }

            // Right Column - Vehicle Details and Timer
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 3.dp),
                horizontalAlignment = Alignment.Start
            ) {
                vehicle?.let {
                    Text(
                        text = "${it.codename}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    VehicleStatusIndicator(status = status)
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    UserDateTimer(
                        modifier = Modifier.fillMaxWidth(),
                        sessionStartTime = startDateTime
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (lastCheck != null) {
                    Text(
                        text = "Check: ${lastCheck.lastCheckDateTime?.let { formatDateTime(it) }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${lastCheck.status}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Press Check In to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

    }
}