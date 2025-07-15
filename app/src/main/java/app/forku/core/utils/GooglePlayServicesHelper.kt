package app.forku.core.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Helper class to handle Google Play Services availability and errors
 */
object GooglePlayServicesHelper {
    private const val TAG = "GooglePlayServices"

    /**
     * Check if Google Play Services is available on the device
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services is available")
                true
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.w(TAG, "Google Play Services is missing")
                false
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.w(TAG, "Google Play Services needs update")
                false
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.w(TAG, "Google Play Services is disabled")
                false
            }
            else -> {
                Log.e(TAG, "Google Play Services error: $resultCode")
                false
            }
        }
    }

    /**
     * Get human-readable error message for Google Play Services issues
     */
    fun getErrorMessage(context: Context): String? {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorString(resultCode)
        } else {
            null
        }
    }

    /**
     * Check if the error can be resolved by user action
     */
    fun isUserResolvableError(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return googleApiAvailability.isUserResolvableError(resultCode)
    }
} 