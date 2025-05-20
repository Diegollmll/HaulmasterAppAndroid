package app.forku.presentation.vehicle.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import app.forku.core.Constants.BASE_URL
import android.util.Log

private const val TAG = "VehicleImage"

@Composable
fun VehicleImage(
    vehicleId: String,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    contentScale: ContentScale = ContentScale.Inside,
    onError: ((Throwable) -> Unit)? = null,
    onSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Mientras el backend no aclare el campo, usamos la URL por vehicleId
    val imageUrl = "${BASE_URL}api/vehicle/file/$vehicleId/Picture?t=%LASTEDITEDTIME%"
    
    Log.d(TAG, "[VehicleImage] Params: vehicleId=$vehicleId, imageLoader=$imageLoader, contentScale=$contentScale")
    Log.d(TAG, "[VehicleImage] Final imageUrl: $imageUrl")

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "Vehicle picture",
        contentScale = contentScale,
        modifier = modifier,
        onError = { 
            Log.e(TAG, "Error loading image for vehicle $vehicleId: $it")
            onError?.invoke(it.result.throwable)
        },
        onSuccess = {
            Log.d(TAG, "Successfully loaded image for vehicle $vehicleId")
            onSuccess?.invoke()
        },
        placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
    )
} 