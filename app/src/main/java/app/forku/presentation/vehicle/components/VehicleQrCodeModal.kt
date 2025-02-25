package app.forku.presentation.vehicle.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.forku.domain.model.vehicle.Vehicle
import app.forku.presentation.common.components.AppModal

@Composable
fun VehicleQrCodeModal(
    vehicle: Vehicle,
    onDismiss: () -> Unit
) {
    AppModal(onDismiss = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vehicle QR Code",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            vehicle.qrCode?.let { code ->
                VehicleQrCode(
                    qrCode = code,
                    modifier = Modifier.size(256.dp)
                )
            } ?: Text(
                text = "No QR Code available",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            //Text(
                //text = vehicle.qrCode,
                //style = MaterialTheme.typography.bodyLarge
            //)
        }
    }
} 