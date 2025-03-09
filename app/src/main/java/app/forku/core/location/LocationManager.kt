package app.forku.core.location

import android.content.Context
import android.os.Looper
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import app.forku.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    private val _locationState = MutableStateFlow(LocationState())
    val locationState = _locationState.asStateFlow()

    fun onLocationPermissionGranted() {
        coroutineScope.launch {
            _locationState.update { it.copy(
                error = null,
                hasLocationPermission = true,
                locationSettingsException = null
            )}
            requestLocationSettings()
        }
    }

    fun onLocationPermissionDenied() {
        _locationState.update { it.copy(
            error = "Location permission is required",
            hasLocationPermission = false,
            locationSettingsException = null
        )}
    }

    fun onLocationSettingsDenied() {
        _locationState.update { it.copy(
            error = "Location services are required",
            locationSettingsException = null
        )}
    }

    private fun requestLocationSettings() {
        try {
            val locationRequest = LocationRequest.Builder(10000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000)
                .build()

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

            LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder)
                .addOnSuccessListener {
                    requestLocation(locationRequest)
                }
                .addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        _locationState.update { it.copy(
                            locationSettingsException = exception,
                            error = "Please enable location services"
                        )}
                    }
                }
        } catch (e: SecurityException) {
            _locationState.update { it.copy(
                error = "Location permission error",
                hasLocationPermission = false
            )}
        }
    }

    private fun requestLocation(locationRequest: LocationRequest) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        _locationState.update { state ->
                            state.copy(
                                location = "${it.latitude}, ${it.longitude}",
                                latitude = it.latitude,
                                longitude = it.longitude,
                                error = null
                            )
                        }
                    } ?: requestLocationUpdates(locationRequest)
                }
                .addOnFailureListener {
                    _locationState.update { it.copy(error = "Failed to get location") }
                }
        } catch (e: SecurityException) {
            _locationState.update { it.copy(error = "Location permission error") }
        }
    }

    private fun requestLocationUpdates(locationRequest: LocationRequest) {
        try {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        _locationState.update { state ->
                            state.copy(
                                location = "${location.latitude}, ${location.longitude}",
                                latitude = location.latitude,
                                longitude = location.longitude,
                                error = null
                            )
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            _locationState.update { it.copy(error = "Location permission error") }
        }
    }

    fun clearError() {
        _locationState.update { it.copy(error = null) }
    }
} 