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
import app.forku.presentation.common.utils.parseDateTime
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import app.forku.domain.model.checklist.getPreShiftStatusColor
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import app.forku.domain.model.user.UserRole
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.presentation.session.SessionViewModel
import app.forku.domain.model.session.VehicleSessionStatus

@Composable
fun SessionCard(
    vehicle: Vehicle?,
    lastCheck: PreShiftCheck?,
    user: User?,
    currentSession: VehicleSession?,
    onCheckClick: ((String) -> Unit)? = null,
    currentUserRole: UserRole = UserRole.OPERATOR,
    onEndSession: ((String) -> Unit)? = null,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val canEndSession = sessionViewModel.canEndSession.collectAsState().value

    // Check if can end session when session or user changes
    LaunchedEffect(currentSession, user) {
        currentSession?.userId?.let { userId ->
            sessionViewModel.checkCanEndSession(userId)
        }
    }

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
            currentSession = currentSession,
            startDateTime = remember(currentSession?.startTime) {
                currentSession?.startTime?.let {
                    try {
                        parseDateTime(it).toLocalDateTime()
                    } catch (e: Exception) {
                        android.util.Log.e("SessionCard", "Error parsing date: $it", e)
                        null
                    }
                }
            },
            lastCheck = lastCheck,
            isActive = currentSession != null,
            onCheckClick = { checkId -> onCheckClick?.invoke(checkId) },
            currentUserRole = currentUserRole,
            canEndSession = canEndSession,
            onEndSession = { currentSession?.id?.let { onEndSession?.invoke(it) } }
        )
    }
}

@Composable
private fun SessionContent(
    vehicle: Vehicle?,
    status: VehicleStatus,
    user: User?,
    currentSession: VehicleSession?,
    startDateTime: LocalDateTime?,
    lastCheck: PreShiftCheck?,
    isActive: Boolean = false,
    onCheckClick: (String) -> Unit,
    currentUserRole: UserRole = UserRole.OPERATOR,
    canEndSession: Boolean = false,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            user?.let {
                if (vehicle != null) {
                    // Left Column - User/Vehicle Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        OverlappingImages(
                            mainImageUrl = vehicle.photoModel,
                            overlayImageUrl = user.photoUrl,
                            mainTint = MaterialTheme.colorScheme.onSurface,
                            mainSize = 120,
                            overlaySize = 60
                        )
                    }
                }
            }

            // Right Column - Vehicle Details and Timer
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                vehicle?.let {
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "${it.codename}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        VehicleStatusIndicator(status = status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        UserDateTimer(
                            modifier = Modifier.fillMaxWidth(),
                            sessionStartTime = startDateTime,
                            isSessionActive = currentSession?.status == VehicleSessionStatus.OPERATING && currentSession.endTime == null
                        )
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
                                        enabled = lastCheck.id != null,
                                        onClick = {
                                            lastCheck.id?.let { onCheckClick(it) }
                                        }
                                    )
                                    .padding(8.dp),
                            ) {
                                Row {
                                    Text(
                                        text = "Checklist",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = modifier.padding(3.dp))

                                    Text(
                                        text = CheckStatus.valueOf(lastCheck.status).toFriendlyString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp
                                        ),
                                        color = getPreShiftStatusColor(lastCheck.status)
                                    )
                                }

                                Row {
                                    Text(
                                        text = "${lastCheck.lastCheckDateTime?.let { getRelativeTimeSpanString(it) }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 13.sp
                                        )
                                    )
                                }
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

        // Add Admin End Session Button
        if (isActive && currentUserRole == UserRole.ADMIN && canEndSession) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onEndSession,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("End Session (Admin)")
            }
        }

    }
}