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

@Composable
fun VehicleProfileSummary() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        border = BorderStroke(1.dp, Color(0xFFFFA726))
    ) {
        // Vehicle Operator Info Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle image on the left
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )

            // Operator details aligned to the right
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Jason Brigg",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(){
                    Text(
                        text = "Pre-Shift Check ",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Expired",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Last Checked: 10:30am",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        // VehicleDetails Section
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
                    text = "ID: 12",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Next Service: 800hrs",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
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
                        text = "Series 386 E12-E20",
                        color = Color.White,
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
                        text = "Diesel",
                        color = Color.White,
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
                        text = "ITA IV",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

    }
}