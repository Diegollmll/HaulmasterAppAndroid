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
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.presentation.vehicle.profile.VehicleProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.checklist.getPreShiftStatusColor
import app.forku.domain.model.checklist.getPreShiftStatusText
import app.forku.presentation.common.utils.formatDateTime
import app.forku.presentation.common.utils.getRelativeTimeSpanString


@Composable
fun VehicleProfileSummary(
    vehicle: Vehicle?,
    status: VehicleStatus,
    activeOperator: User? = null,
    showOperatorDetails: Boolean = true,
    showPreShiftCheckDetails: Boolean = true,
    showVehicleDetails: Boolean = true,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
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
                    Text(
                        text = vehicle?.codename ?: "No vehicle name",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    VehicleStatusIndicator(status = status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle image and details
            VehicleDetailsSection(
                vehicle = vehicle,
                showPreShiftCheckDetails = showPreShiftCheckDetails,
                activeOperator = activeOperator,
                showOperatorDetails = showOperatorDetails,
                showVehicleDetails = showVehicleDetails,
                status = status,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun OperatorProfile(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    role: String = "Operator"
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
        // Profile Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile picture of $name",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        // User info
        Column {
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun VehicleDetailsSection(
    vehicle: Vehicle?,
    showPreShiftCheckDetails: Boolean,
    activeOperator: User? = null,
    showOperatorDetails: Boolean = true,
    showVehicleDetails: Boolean = true,
    status: VehicleStatus,
    viewModel: VehicleProfileViewModel
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
        AsyncImage(
            model = vehicle?.photoModel,
            contentDescription = "Vehicle image",
            modifier = Modifier
                .size(136.dp),
            contentScale = ContentScale.Fit
        )

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
                    activeOperator?.role?.let {
                        OperatorProfile(
                            name = activeOperator?.name ?: "No operator assigned",
                            imageUrl = activeOperator.photoUrl,
                            modifier = Modifier.padding(0.dp, 8.dp),
                            role = it.name
                        )
                    }
                }
            }

//            Text(
//                text = "Vehicle",
//                color = Color.Gray,
//                fontSize = 12.sp
//            )
//            Text(
//                text = "${vehicle?. type?.displayName}",
//                color = Color.Gray,
//                fontSize = 12.sp
//            )
            //&& lastCheck.value?.lastCheckDateTime != null
            if (showPreShiftCheckDetails) {
                Row {
                    Column {
                        Text(
                            text = "Pre-Shift Check ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))
                }
                Row {
                    Column {
                        Text(
                            text = getPreShiftStatusText(status = lastCheck?.value?.status ?: ""),
                            color = getPreShiftStatusColor(status = lastCheck?.value?.status ?: ""),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = lastCheck?.value?.lastCheckDateTime?.let { 
                        getRelativeTimeSpanString(it) 
                    } ?: "No checks found.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }

    if (showVehicleDetails){
        // Lower section with border
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFFFA726))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                // ID and Next Service, Model, Type, and Class Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            text = "Type",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = vehicle?.type?.displayName ?: "Unknown",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Model",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = vehicle?.model ?: "Unknown",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Energy",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = vehicle?.energyType ?: "Unknown",
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }



                    Column {
                        Text(
                            text = "Next Service",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${vehicle?.nextService ?: "Unknown"} hrs",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

}


