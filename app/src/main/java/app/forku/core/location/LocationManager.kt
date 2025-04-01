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
            android.util.Log.d("LocationManager", "Requesting last location")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        android.util.Log.d("LocationManager", "Received location update: lat=${it.latitude}, lon=${it.longitude}")
                        val locationString = "${it.latitude},${it.longitude}"
                        android.util.Log.d("LocationManager", "Formatted location string: $locationString")
                        _locationState.update { state ->
                            state.copy(
                                location = locationString,
                                latitude = it.latitude,
                                longitude = it.longitude,
                                error = null
                            )
                        }
                        android.util.Log.d("LocationManager", "Updated location state: ${_locationState.value}")
                    } ?: run {
                        android.util.Log.w("LocationManager", "Last location is null, requesting updates")
                        requestLocationUpdates(locationRequest)
                    }
                }
                .addOnFailureListener {
                    android.util.Log.e("LocationManager", "Failed to get last location", it)
                    _locationState.update { it.copy(error = "Failed to get location") }
                }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationManager", "Security exception while getting location", e)
            _locationState.update { it.copy(error = "Location permission error") }
        }
    }

    private fun requestLocationUpdates(locationRequest: LocationRequest) {
        try {
            android.util.Log.d("LocationManager", "Setting up location updates")
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        android.util.Log.d("LocationManager", "Received location update from callback: lat=${location.latitude}, lon=${location.longitude}")
                        val locationString = "${location.latitude},${location.longitude}"
                        android.util.Log.d("LocationManager", "Formatted location string: $locationString")
                        _locationState.update { state ->
                            state.copy(
                                location = locationString,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                error = null
                            )
                        }
                        android.util.Log.d("LocationManager", "Updated location state: ${_locationState.value}")
                        fusedLocationClient.removeLocationUpdates(this)
                        android.util.Log.d("LocationManager", "Removed location updates after receiving location")
                    } ?: android.util.Log.w("LocationManager", "Location result is null")
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            android.util.Log.d("LocationManager", "Location updates requested")
        } catch (e: SecurityException) {
            android.util.Log.e("LocationManager", "Security exception while requesting location updates", e)
            _locationState.update { it.copy(error = "Location permission error") }
        }
    }

    fun clearError() {
        _locationState.update { it.copy(error = null) }
    }

    fun startLocationUpdates() {
        coroutineScope.launch {
            try {
                val locationRequest = LocationRequest.Builder(10000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(5000)
                    .build()

                requestLocationSettings()
            } catch (e: SecurityException) {
                _locationState.update { it.copy(
                    error = "Location permission error",
                    hasLocationPermission = false
                )}
            }
        }
    }

    fun requestSingleUpdate() {
        try {
            val locationRequest = LocationRequest.Builder(10000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build()

            requestLocation(locationRequest)
        } catch (e: SecurityException) {
            _locationState.update { it.copy(
                error = "Location permission error",
                hasLocationPermission = false
            )}
        }
    }
} 