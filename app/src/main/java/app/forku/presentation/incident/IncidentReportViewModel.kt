package app.forku.presentation.incident

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.usecase.incident.ReportIncidentUseCase
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.weather.WeatherRepository
import app.forku.presentation.incident.model.IncidentFormSection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest

@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val sessionRepository: SessionRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentReportState())
    val state = _state.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        loadCurrentSession()
        // Try to get location immediately if permissions are already granted
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            onLocationPermissionGranted()
        }
    }

    private fun loadCurrentSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getCurrentSession()
                _state.update { 
                    it.copy(
                        vehicleId = session?.vehicleId,
                        sessionId = session?.id
                    )
                }
            } catch (e: Exception) {
                // Session not required for incident reporting
                android.util.Log.w("Incident", "No active session", e)
            }
        }
    }

    fun setType(type: IncidentType) {
        _state.update { it.copy(type = type) }
    }

    fun setIncidentType(incidentType: String) {
        try {
            val type = IncidentType.valueOf(incidentType.uppercase().replace(" ", "_"))
            _state.update { it.copy(type = type) }
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(error = "Invalid incident type") }
        }
    }

    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun submitReport() {
        val currentState = state.value
        
        if (currentState.type == null) {
            _state.update { it.copy(error = "Please select an incident type") }
            return
        }

        if (currentState.description.isBlank()) {
            _state.update { it.copy(error = "Please provide a description") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                reportIncidentUseCase(
                    type = currentState.type,
                    description = currentState.description
                )
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isSubmitted = true,
                        showSuccessDialog = true
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to submit report"
                    )
                }
            }
        }
    }

    fun resetForm() {
        _state.value = IncidentReportState()
    }

    fun dismissSuccessDialog() {
        _state.update { it.copy(showSuccessDialog = false) }
    }

    fun updateState(newState: IncidentReportState) {
        _state.value = newState
    }

    fun nextSection() {
        val currentState = state.value
        val nextSection = when (currentState.currentSection) {
            IncidentFormSection.BasicInfo -> IncidentFormSection.PeopleInvolved
            IncidentFormSection.PeopleInvolved -> IncidentFormSection.VehicleInfo
            IncidentFormSection.VehicleInfo -> IncidentFormSection.IncidentDetails
            IncidentFormSection.IncidentDetails -> IncidentFormSection.RootCauseAnalysis
            IncidentFormSection.RootCauseAnalysis -> IncidentFormSection.Documentation
            IncidentFormSection.Documentation -> IncidentFormSection.Documentation // Stay on last section
        }
        _state.update { it.copy(currentSection = nextSection) }
    }

    fun previousSection() {
        val currentState = state.value
        val previousSection = when (currentState.currentSection) {
            IncidentFormSection.Documentation -> IncidentFormSection.RootCauseAnalysis
            IncidentFormSection.RootCauseAnalysis -> IncidentFormSection.IncidentDetails
            IncidentFormSection.IncidentDetails -> IncidentFormSection.VehicleInfo
            IncidentFormSection.VehicleInfo -> IncidentFormSection.PeopleInvolved
            IncidentFormSection.PeopleInvolved -> IncidentFormSection.BasicInfo
            IncidentFormSection.BasicInfo -> IncidentFormSection.BasicInfo // Stay on first section
        }
        _state.update { it.copy(currentSection = previousSection) }
    }

    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            try {
                val locationRequest = LocationRequest.Builder(10000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(5000)
                    .build()

                val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true)
                    .build()

                val client = LocationServices.getSettingsClient(context)
                
                client.checkLocationSettings(builder)
                    .addOnSuccessListener {
                        android.util.Log.d("ViewModel", "Location settings satisfied")
                        requestLocation(locationRequest)
                    }
                    .addOnFailureListener { exception ->
                        android.util.Log.e("ViewModel", "Location settings check failed", exception)
                        if (exception is ResolvableApiException) {
                            // Instead of trying to handle resolution here, expose it to the UI
                            _state.update { it.copy(
                                locationSettingsException = exception,
                                error = "Please enable location services"
                            )}
                        }
                    }
            } catch (e: SecurityException) {
                android.util.Log.e("ViewModel", "Security exception getting location", e)
                _state.update { it.copy(error = "Location permission error") }
            }
        }
    }

    private fun requestLocation(locationRequest: LocationRequest) {
        try {
            android.util.Log.d("ViewModel", "Requesting location updates")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val coordinates = "${location.latitude}, ${location.longitude}"
                        android.util.Log.d("ViewModel", "Got last location: $coordinates")
                        fetchWeather(location.latitude, location.longitude)
                        _state.update { state ->
                            state.copy(
                                location = coordinates,
                                hasLocationPermission = true
                            )
                        }
                    } else {
                        android.util.Log.d("ViewModel", "Last location null, requesting updates")
                        // Last location was null, request updates
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult) {
                                    val latestLocation = locationResult.lastLocation
                                    if (latestLocation != null) {
                                        val coordinates = "${latestLocation.latitude}, ${latestLocation.longitude}"
                                        android.util.Log.d("ViewModel", "New location received: $coordinates")
                                        _state.update { state ->
                                            state.copy(
                                                location = coordinates,
                                                hasLocationPermission = true
                                            )
                                        }
                                        fusedLocationClient.removeLocationUpdates(this)
                                        fetchWeather(latestLocation.latitude, latestLocation.longitude)
                                    }
                                }
                            },
                            null
                        )
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("ViewModel", "Failed to get last location", e)
                    _state.update { it.copy(error = "Failed to get location") }
                }
        } catch (e: SecurityException) {
            android.util.Log.e("ViewModel", "Security exception in requestLocation", e)
            _state.update { it.copy(error = "Location permission error") }
        }
    }

    fun onLocationPermissionDenied() {
        _state.update { it.copy(
            error = "Location permission is required for accurate incident reporting"
        )}
    }

    fun onLocationSettingsDenied() {
        _state.update { it.copy(
            error = "Location services are required for accurate incident reporting",
            locationSettingsException = null
        )}
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepository.getWeatherByCoordinates(latitude, longitude)
                .onSuccess { weather ->
                    val weatherDescription = "${weather.description}, Temperature: ${weather.temperature}°C, " +
                        "Humidity: ${weather.humidity}%, Wind: ${weather.windSpeed} m/s"
                    _state.update { it.copy(weather = weatherDescription) }
                }
                .onFailure { error ->
                    android.util.Log.e("Weather", "Failed to fetch weather", error)
                }
        }
    }
} 