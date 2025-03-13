package app.forku.presentation.vehicle.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.forku.presentation.common.components.AppModal

@Composable
fun VehicleQrCodeModal(
    vehicleId: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier.size(256.dp)
) {
    AppModal(
        onDismiss = onDismiss,
        onConfirm = onDismiss,
        title = "Vehicle QR Code",
        message = "Scan this QR code to access vehicle information",
        confirmText = "Accept",
        dismissText = "Cancel",
        content = {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VehicleQrCode(
                    vehicleId = vehicleId,
                    modifier = Modifier.size(256.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share QR Code",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share QR Code")
                }
            }
        }
    )
} 