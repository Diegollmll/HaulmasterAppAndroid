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
        onConfirm = onShare,
        title = "Vehicle QR Code",
        message = "Scan this QR code to access vehicle information",
        confirmText = "Share",
        dismissText = "Close",
        content = {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VehicleQrCode(
                    vehicleId = vehicleId,
                    modifier = Modifier.size(256.dp)
                )
            }
        }
    )
} 