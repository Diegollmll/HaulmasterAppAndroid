import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onPermissionsGranted()
        } else if (locationPermissionsState.permissions.any { !it.status.isGranted }) {
            onPermissionsDenied()
        }
    }
}

@Composable
fun YourScreen() {
    var hasLocationPermissions by remember { mutableStateOf(false) }

    LocationPermissionHandler(
        onPermissionsGranted = {
            hasLocationPermissions = true
            // Initialize location updates here
        },
        onPermissionsDenied = {
            // Handle denied permissions
            // Show a message or alternative functionality
        }
    )

    // Rest of your screen content
} 