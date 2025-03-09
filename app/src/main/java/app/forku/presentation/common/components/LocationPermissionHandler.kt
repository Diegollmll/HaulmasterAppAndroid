package app.forku.presentation.common.components

import android.Manifest
import android.content.IntentSender
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.common.api.ResolvableApiException

private const val LOCATION_SETTINGS_REQUEST = 1001

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    locationSettingsException: ResolvableApiException? = null,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit,
    onLocationSettingsDenied: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var hasShownDialog by remember { mutableStateOf(false) }

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onPermissionsGranted()
        } else if (!hasShownDialog) {
            hasShownDialog = true
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            onPermissionsDenied()
        }
    }

    LaunchedEffect(locationSettingsException) {
        locationSettingsException?.let { exception ->
            activity?.let { nonNullActivity ->
                try {
                    exception.startResolutionForResult(nonNullActivity, LOCATION_SETTINGS_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                    onLocationSettingsDenied()
                }
            }
        }
    }
}
