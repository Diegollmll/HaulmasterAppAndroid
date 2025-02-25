package app.forku.presentation.vehicle.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.user.User

@Composable
fun VehicleProfileSummary(
    vehicle: Vehicle,
    operator: User? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = null
    ) {
        // Upper section (no border)
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Operator",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = operator?.name ?: "No operator assigned",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    text = "Last Checked: ${lastCheck?.datetime}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
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
                        text = "ID: ${vehicle.codename}",
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