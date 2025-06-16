package app.forku.presentation.vehicle.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.VehicleStatus
import coil.request.ImageRequest
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.presentation.vehicle.profile.VehicleProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.checklist.getPreShiftStatusColor
import app.forku.domain.model.checklist.getPreShiftStatusText
import app.forku.presentation.common.utils.formatDateTime
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import app.forku.presentation.common.components.UserDateTimer
import app.forku.presentation.common.utils.parseDateTime
import coil.ImageLoader
import app.forku.presentation.vehicle.components.VehicleImage
import app.forku.domain.model.checklist.CheckStatus
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar

@Composable
fun VehicleProfileSummary(
    vehicle: Vehicle?,
    status: VehicleStatus,
    activeOperator: User? = null,
    lastOperator: User? = null,
    showOperatorDetails: Boolean = true,
    showPreShiftCheckDetails: Boolean = true,
    showVehicleDetails: Boolean = true,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    imageLoader: ImageLoader,
    viewModel: VehicleProfileViewModel = hiltViewModel()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row {
                Column {
                    Row {
                        Text(
                            text = vehicle?.codename ?: "No vehicle name",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            VehicleStatusIndicator(status = status)
                        }

                    }
                    Row {
                        Text(
                            text = vehicle?.type?.Name ?: "Unknown type",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }


            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle image and details
            VehicleDetailsSection(
                vehicle = vehicle,
                showPreShiftCheckDetails = showPreShiftCheckDetails,
                activeOperator = activeOperator,
                lastOperator = lastOperator,
                showOperatorDetails = showOperatorDetails,
                showVehicleDetails = showVehicleDetails,
                status = status,
                viewModel = viewModel,
                imageLoader = imageLoader
            )
        }
    }
}

@Composable
fun OperatorProfile(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    role: String = "Operator",
    avatarSize: Dp = 40.dp,
    fontSize: TextUnit = 18.sp,
    roleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            avatarData = getUserAvatarData(name.split(" ").firstOrNull(), name.split(" ").drop(1).firstOrNull(), imageUrl),
            size = avatarSize,
            fontSize = fontSize
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = roleColor,
                fontSize = fontSize * 0.7f
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun VehicleDetailsSection(
    vehicle: Vehicle?,
    showPreShiftCheckDetails: Boolean,
    activeOperator: User? = null,
    lastOperator: User? = null,
    showOperatorDetails: Boolean = true,
    showVehicleDetails: Boolean = true,
    status: VehicleStatus,
    viewModel: VehicleProfileViewModel,
    imageLoader: ImageLoader
) {
    val scope = rememberCoroutineScope()
    var lastCheck = remember { mutableStateOf<PreShiftCheck?>(null) }

    // Load last check when vehicle changes
    LaunchedEffect(vehicle?.id) {
        vehicle?.id?.let { vehicleId ->
            lastCheck.value = viewModel.getLastPreShiftCheck(vehicleId)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {

        if (vehicle != null) {
            VehicleImage(
                vehicleId = vehicle.id,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                imageLoader = imageLoader,
                contentScale = ContentScale.Fit
            )
        }

        Divider(
            modifier = Modifier
                .height(90.dp)
                .width(1.dp),
            color = Color.LightGray
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Operator
            if (showOperatorDetails) {
                Column(
                    modifier = Modifier.padding(vertical = 0.dp)
                ) {
                    when {
                        activeOperator?.role != null -> {
                            val displayName = when {
                                !activeOperator.firstName.isNullOrBlank() || !activeOperator.lastName.isNullOrBlank() ->
                                    listOfNotNull(activeOperator.firstName, activeOperator.lastName).joinToString(" ").trim()
                                !activeOperator.username.isNullOrBlank() -> activeOperator.username
                                else -> "No name"
                            }
                            // android.util.Log.d("VehicleProfileSummary", "Mostrando operador ACTIVO: $displayName")
                            OperatorProfile(
                                name = displayName,
                                imageUrl = activeOperator.photoUrl,
                                modifier = Modifier.padding(0.dp, 8.dp),
                                role = activeOperator.role.name
                            )
                            
                            // Add session duration timer when there is an active operator
                            if (status == VehicleStatus.IN_USE && viewModel.state.value.activeSession != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Current session:",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val sessionStartInstant = remember(viewModel.state.value.activeSession?.startTime) {
                                        viewModel.state.value.activeSession?.startTime?.let {
                                            try {
                                                parseDateTime(it).toInstant()
                                            } catch (e: Exception) {
                                                android.util.Log.e("VehicleProfileSummary", "Error parsing date: $it", e)
                                                null
                                            }
                                        }
                                    }
                                    UserDateTimer(
                                        sessionStartInstant = sessionStartInstant,
                                        fontSize = 16
                                    )
                                }
                            }
                            // Mostrar también el último operador si existe y es diferente al actual
                            if (lastOperator != null && lastOperator.id != activeOperator.id) {
                                val lastDisplayName = when {
                                    !lastOperator.firstName.isNullOrBlank() || !lastOperator.lastName.isNullOrBlank() ->
                                        listOfNotNull(lastOperator.firstName, lastOperator.lastName).joinToString(" ").trim()
                                    !lastOperator.username.isNullOrBlank() -> lastOperator.username
                                    else -> "No name"
                                }
                                // android.util.Log.d("VehicleProfileSummary", "Mostrando ULTIMO operador: $lastDisplayName")
                                Spacer(modifier = Modifier.height(8.dp))
                                OperatorProfile(
                                    name = lastDisplayName,
                                    imageUrl = lastOperator.photoUrl,
                                    modifier = Modifier.padding(0.dp, 8.dp),
                                    role = "Last ${lastOperator.role.name}",
                                    avatarSize = 28.dp,
                                    fontSize = 12.sp,
                                    roleColor = Color.Gray
                                )
                            }
                        }
                        lastOperator != null -> {
                            val lastDisplayName = when {
                                !lastOperator.firstName.isNullOrBlank() || !lastOperator.lastName.isNullOrBlank() ->
                                    listOfNotNull(lastOperator.firstName, lastOperator.lastName).joinToString(" ").trim()
                                !lastOperator.username.isNullOrBlank() -> lastOperator.username
                                else -> "No name"
                            }
                            // android.util.Log.d("VehicleProfileSummary", "Mostrando SOLO ULTIMO operador: $lastDisplayName")
                            OperatorProfile(
                                name = lastDisplayName,
                                imageUrl = lastOperator.photoUrl,
                                modifier = Modifier.padding(0.dp, 8.dp),
                                role = "Last ${lastOperator.role.name}",
                                avatarSize = 28.dp,
                                fontSize = 12.sp,
                                roleColor = Color.Gray
                            )
                        }
                        viewModel.state.value.lastChecklistOperator != null -> {
                            val lastChecklistOperator = viewModel.state.value.lastChecklistOperator!!
                            val checklistDisplayName = when {
                                !lastChecklistOperator.firstName.isNullOrBlank() || !lastChecklistOperator.lastName.isNullOrBlank() ->
                                    listOfNotNull(lastChecklistOperator.firstName, lastChecklistOperator.lastName).joinToString(" ").trim()
                                !lastChecklistOperator.username.isNullOrBlank() -> lastChecklistOperator.username
                                else -> "No name"
                            }
                            // android.util.Log.d("VehicleProfileSummary", "Mostrando operador de CHECKLIST: $checklistDisplayName")
                            OperatorProfile(
                                name = checklistDisplayName,
                                imageUrl = lastChecklistOperator.photoUrl,
                                modifier = Modifier.padding(0.dp, 8.dp),
                                avatarSize = 0.dp,
                                role = "Last Operator"
                            )
                        }
                        else -> {
                            // android.util.Log.d("VehicleProfileSummary", "No hay historial de operador")
                            Text(
                                text = "No operator history",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (showPreShiftCheckDetails) {
                Row {
                    Column {
                        Text(
                            text = "Checklist ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Column {
                        val lastChecklistAnswer = viewModel.state.value.lastChecklistAnswer
                        
                        // Display checklist status
                        if (lastChecklistAnswer != null) {
                            val statusText = getPreShiftStatusText(lastChecklistAnswer.status)
                            val statusColor = getPreShiftStatusColor(statusText)
                            
                            Text(
                                text = "Checklist: $statusText",
                                style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                                modifier = Modifier.padding(top = 2.dp)
                        )
                        }
                    }

                }
                Row {
                    val lastChecklistAnswer = viewModel.state.value.lastChecklistAnswer
//                    android.util.Log.d(
//                        "VehicleProfileSummary",
//                        "Checklist date debug: lastCheckDateTime=${lastChecklistAnswer?.lastCheckDateTime}, endDateTime=${lastChecklistAnswer?.endDateTime}, lastCheckStartDateTime=${lastCheck?.value?.startDateTime}"
//                    )
                    Text(
                        text = lastChecklistAnswer?.lastCheckDateTime?.takeIf { it.isNotBlank() }?.let {
                            getRelativeTimeSpanString(it)
                        } ?: lastChecklistAnswer?.endDateTime?.takeIf { it.isNotBlank() }?.let {
                            getRelativeTimeSpanString(it)
                        } ?: lastCheck?.value?.startDateTime?.let {
                            getRelativeTimeSpanString(it)
                        } ?: "No checks found.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showVehicleDetails){
        // Lower section with border
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                // ID and Next Service, Model, Type, and Class Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Model",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = vehicle?.model ?: "Unknown",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Energy",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = vehicle?.energyType ?: "Unknown",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Next Service",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${vehicle?.nextService ?: "Unknown"} hrs",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

}


