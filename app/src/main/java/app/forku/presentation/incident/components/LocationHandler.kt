package app.forku.presentation.incident.components

import LocationPermissionHandler
import android.Manifest
import android.app.Activity
import android.content.IntentSender
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.common.api.ResolvableApiException

private const val LOCATION_SETTINGS_REQUEST = 1001

@Composable
fun LocationHandler(
    locationSettingsException: ResolvableApiException?,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(locationSettingsException) {
        locationSettingsException?.let { exception ->
            activity?.let { nonNullActivity ->
                try {
                    exception.startResolutionForResult(nonNullActivity, LOCATION_SETTINGS_REQUEST)
                } catch (sendEx: IntentSender.SendIntentException) {
                    android.util.Log.e("LocationHandler", "Error showing location settings dialog", sendEx)
                }
            }
        }
    }

    LocationPermissionHandler(
        onPermissionsGranted = onPermissionsGranted,
        onPermissionsDenied = onPermissionsDenied
    )
} 