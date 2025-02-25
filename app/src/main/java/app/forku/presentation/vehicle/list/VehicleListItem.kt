package app.forku.presentation.vehicle.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale



@Composable
fun VehicleListItem(
    vehicle: Vehicle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ID: ${vehicle.codename}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "•",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = vehicle.status,
                        color = when(vehicle.status) {
                            VehicleStatus.CHECKED_IN.toString() -> Color(0xFF4CAF50)
                            VehicleStatus.IN_USE.toString() -> Color(0xFFFFA726)
                            else -> Color.Red
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = vehicle.type.name,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            android.util.Log.d("VehicleListItem", "PhotoModel URL: ${vehicle.photoModel}")
            AsyncImage(
                model = vehicle.photoModel,
                contentDescription = "Vehicle image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                onError = { 
                    android.util.Log.e("VehicleListItem", "Error loading image: ${vehicle.photoModel}")
                }
            )

        }
    }
} 