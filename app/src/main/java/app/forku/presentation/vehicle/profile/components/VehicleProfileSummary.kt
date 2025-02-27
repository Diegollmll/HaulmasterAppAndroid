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
import app.forku.R
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.VehicleStatus
import coil.request.ImageRequest


@Composable
fun VehicleProfileSummary(
    vehicle: Vehicle,
    status: VehicleStatus,
    activeOperator: User? = null,
    showOperatorDetails: Boolean = true,
    showFullDetails: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = vehicle.codename,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            VehicleStatusIndicator(status = status)

            // Vehicle image and details
            VehicleDetailsSection(
                vehicle = vehicle,
                showFullDetails = showFullDetails,
                activeOperator = activeOperator,
                showOperatorDetails = showOperatorDetails,
                status = status
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
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
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

        Spacer(modifier = Modifier.width(12.dp))

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
    vehicle: Vehicle,
    showFullDetails: Boolean = true,
    activeOperator: User? = null,
    showOperatorDetails: Boolean = true,
    status: VehicleStatus
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = vehicle.photoModel,
            contentDescription = "Vehicle image",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Divider(
            modifier = Modifier
                .height(90.dp)
                .width(1.dp),
            color = Color.LightGray
        )

//        if (showOperatorDetails && status == VehicleStatus.IN_USE) {
//            Column(
//                modifier = Modifier.padding(vertical = 8.dp)
//            ) {
//                activeOperator?.role?.let {
//                    OperatorProfile(
//                        name = activeOperator?.name ?: "No operator assigned",
//                        imageUrl = activeOperator.photoUrl,
//                        modifier = Modifier.padding(16.dp),
//                        role = it.name
//                    )
//                }
//            }
//        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            if (showFullDetails) {
                Text(
                    text = "Pre-Shift Check",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                val lastCheck = vehicle.checks?.lastOrNull()
                Text(
                    text = getPreShiftStatusText(lastCheck?.status ?: ""),
                    color = getPreShiftStatusColor(lastCheck?.status ?: ""),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Last Checked: ${lastCheck?.lastcheck_datetime}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }

    // Lower section with border
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFFFA726))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ID and Next Service Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.type.displayName,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Next Service",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${vehicle.nextService} hrs",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Model, Type, and Class Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Model",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = vehicle.model,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(
                        text = "Type",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = vehicle.energyType,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(
                        text = "Class",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = vehicle.vehicleClass,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun getPreShiftStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "PENDING" -> Color.Gray
        "IN_PROGRESS" -> Color(0xFFFFA726) // Orange
        "COMPLETED_PASS" -> Color(0xFF4CAF50) // Green
        "COMPLETED_FAIL" -> Color.Red
        "EXPIRED" -> Color.Red
        "OVERDUE" -> Color.Red
        else -> Color.Gray
    }
}

@Composable
fun getPreShiftStatusText(status: String): String {
    return when (status.uppercase()) {
        "PENDING" -> "Pending"
        "IN_PROGRESS" -> "In Progress"
        "COMPLETED_PASS" -> "PASS"
        "COMPLETED_FAIL" -> "FAIL"
        "EXPIRED" -> "Expired"
        "OVERDUE" -> "Overdue"
        else -> status
    }
}

@Composable
fun VehicleStatusIndicator(status: VehicleStatus) {
    val (color, text) = when (status) {
        VehicleStatus.AVAILABLE -> Color.Green to "Available"
        VehicleStatus.IN_USE -> Color.Blue to "In Use"
        VehicleStatus.BLOCKED -> Color.Red to "Blocked"
        VehicleStatus.UNKNOWN -> Color.Gray to "Unknown"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun VehicleStatus.toDisplayString(): String = when (this) {
    VehicleStatus.IN_USE -> "In Use"
    VehicleStatus.BLOCKED -> "Blocked"
    VehicleStatus.AVAILABLE -> "Available"
    VehicleStatus.UNKNOWN -> "Unknown"
}

private fun VehicleStatus.toColor(): Color = when (this) {
    VehicleStatus.IN_USE -> Color(0xFF6CAFD0)
    VehicleStatus.BLOCKED -> Color(0xFFF64A3A)
    VehicleStatus.AVAILABLE -> Color(0xFF21F6F5)
    VehicleStatus.UNKNOWN -> Color(0xFFFFFF9800)
}