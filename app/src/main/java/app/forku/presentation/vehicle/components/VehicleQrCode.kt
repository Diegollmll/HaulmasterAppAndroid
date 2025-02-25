package app.forku.presentation.vehicle.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrCodeGenerator {
    fun generateVehicleQrCode(
        vehicleId: String,
        size: Int = 512,
        errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.H
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to errorCorrection,
            EncodeHintType.MARGIN to 2
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            vehicleId,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        return bitmap
    }
}

@Composable
fun VehicleQrCode(
    qrCode: String,
    modifier: Modifier = Modifier,
    size: Int = 512,
    errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.H
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(qrCode) {
        withContext(Dispatchers.Default) {
            bitmap = QrCodeGenerator.generateVehicleQrCode(
                vehicleId = qrCode,
                size = size,
                errorCorrection = errorCorrection
            )
        }
    }
    
    bitmap?.let { qrBitmap ->
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "QR Code for vehicle $qrCode",
            modifier = modifier
                .aspectRatio(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(8.dp)
        )
    } ?: Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
} 