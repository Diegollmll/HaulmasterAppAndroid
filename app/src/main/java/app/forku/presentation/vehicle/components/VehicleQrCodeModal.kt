package app.forku.presentation.vehicle.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.presentation.common.components.AppModal

@Composable
fun VehicleQrCodeModal(
    vehicleId: String,
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

            VehicleQrCode(
                vehicleId = vehicleId,
                modifier = Modifier.size(256.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
} 