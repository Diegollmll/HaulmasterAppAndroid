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
import com.google.android.gms.common.api.ResolvableApiException
import app.forku.domain.repository.user.AuthRepository
import android.net.Uri
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import app.forku.domain.repository.checklist.ChecklistRepository


@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val sessionRepository: SessionRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository,
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentReportState())
    val state = _state.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    var tempPhotoUri: Uri? = null
        private set

    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard = _navigateToDashboard.asStateFlow()

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
                val currentUser = authRepository.getCurrentUser()
                
                _state.update { 
                    it.copy(
                        vehicleId = session?.vehicleId,
                        sessionId = session?.id,
                        operatorId = currentUser?.id
                    )
                }
                
                session?.vehicleId?.let { vehicleId ->
                    try {
                        val vehicle = vehicleRepository.getVehicle(vehicleId)
                        // Get the last preshift check from repository
                        val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                        
                        // Convert string date to LocalDateTime
                        val lastCheckDate = lastCheck?.lastCheckDateTime?.let { dateString ->
                            try {
                                LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        _state.update { currentState ->
                            currentState.copy(
                                vehicleType = vehicle.type,
                                vehicleName = vehicle.codename,
                                lastPreshiftCheck = lastCheckDate,
                                preshiftCheckStatus = lastCheck?.status.toString()
                            )
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = "Failed to load vehicle details") }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load session") }
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

    fun onSubmit() {
        viewModelScope.launch {
            when (val validationResult = state.value.validate()) {
                is ValidationResult.Success -> {
                    _state.update { it.copy(isLoading = true) }
                    
                    try {
                        val result = reportIncidentUseCase(
                            type = state.value.type ?: throw IllegalStateException("Incident type is required"),
                            date = state.value.date,
                            location = state.value.location,
                            locationDetails = state.value.locationDetails,
                            weather = state.value.weather,
                            description = state.value.description,
                            incidentTime = state.value.incidentTime,
                            severityLevel = state.value.severityLevel,
                            preshiftCheckStatus = state.value.preshiftCheckStatus,
                            typeSpecificFields = state.value.typeSpecificFields,
                            sessionId = state.value.sessionId,
                            operatorId = state.value.operatorId,
                            othersInvolved = state.value.othersInvolved,
                            injuries = state.value.injuries,
                            injuryLocations = state.value.injuryLocations,
                            vehicleId = state.value.vehicleId,
                            vehicleType = state.value.vehicleType,
                            vehicleName = state.value.vehicleName,
                            photos = state.value.photos,
                            locationCoordinates = state.value.locationCoordinates
                        )

                        result.onSuccess {
                            _state.update { it.copy(
                                isLoading = false,
                                showSuccessDialog = true
                            ) }
                            _navigateToDashboard.value = true
                        }.onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to submit incident report"
                            ) }
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to submit incident report"
                        ) }
                    }
                }
                is ValidationResult.Error -> {
                    _state.update { it.copy(error = validationResult.message) }
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
                    val weatherDescription = "${weather.description}, ${weather.temperature}°C"
                    _state.update { it.copy(weather = weatherDescription) }
                }
                .onFailure { error ->
                    android.util.Log.e("Weather", "Failed to fetch weather", error)
                }
        }
    }

    fun addPhoto(uri: Uri) {
        _state.update { currentState ->
            currentState.copy(
                photos = currentState.photos + uri
            )
        }
    }

    fun createTempPhotoUri(context: Context): Uri? {
        return try {
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val photoFile = java.io.File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                context.cacheDir
            )
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            ).also { tempPhotoUri = it }
        } catch (e: Exception) {
            android.util.Log.e("Camera", "Error creating photo file", e)
            null
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetNavigation() {
        _navigateToDashboard.value = false
    }
} 