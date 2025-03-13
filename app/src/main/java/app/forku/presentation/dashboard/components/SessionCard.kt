package app.forku.presentation.dashboard.components

import androidx.compose.foundation.clickable
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
import androidx.core.graphics.toColorInt
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.VehicleSession
import app.forku.domain.model.vehicle.toColor
import app.forku.domain.model.vehicle.toDisplayString
import app.forku.presentation.common.utils.formatDateTime
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import app.forku.presentation.common.utils.getRelativeTimeSpanString


@Composable
fun SessionCard(
    vehicle: Vehicle?,
    lastCheck: PreShiftCheck?,
    user: User?,
    currentSession: VehicleSession?,
    onCheckClick: ((String) -> Unit)? = null
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
            isActive = currentSession != null,
            onCheckClick = { checkId -> onCheckClick?.invoke(checkId) }
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
    onCheckClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        
        Text(
            text = if (isActive) "Active Session" else "Welcome ${user?.firstName ?: ""}!",
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = lastCheck.status == CheckStatus.IN_PROGRESS.toString() && onCheckClick != null,
                                onClick = { lastCheck.id?.let { onCheckClick(it) } }
                            )
                            .padding(8.dp),
                    ) {

                        Text(
                            text = "Check: ${lastCheck.lastCheckDateTime?.let { getRelativeTimeSpanString(it) }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = lastCheck.status,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp
                            )
                        )

                    }

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