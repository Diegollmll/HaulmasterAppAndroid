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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.domain.repository.user.AuthRepository
import android.net.Uri
import app.forku.core.location.LocationManager
import app.forku.core.location.LocationState
import app.forku.domain.repository.vehicle.VehicleRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import app.forku.domain.repository.checklist.ChecklistRepository

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted


@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val sessionRepository: SessionRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val vehicleRepository: VehicleRepository,
    private val checklistRepository: ChecklistRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentReportState())
    val state = _state.asStateFlow()

    val locationState = locationManager.locationState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LocationState()
    )

    var tempPhotoUri: Uri? = null
        private set

    private val _navigateToDashboard = MutableStateFlow(false)
    val navigateToDashboard = _navigateToDashboard.asStateFlow()

    init {
        loadCurrentSession()
        checkLocationPermission()
        observeLocationUpdates()
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationState.collect { locationStateValue ->
                locationStateValue.location?.let { location ->
                    _state.update { it.copy(
                        location = location,
                        locationCoordinates = location
                    )}
                }
                locationStateValue.error?.let { error ->
                    _state.update { it.copy(error = error) }
                }
                if (locationStateValue.latitude != null && locationStateValue.longitude != null) {
                    fetchWeather(locationStateValue.latitude, locationStateValue.longitude)
                }
            }
        }
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
                            isLoadCarried = state.value.isLoadCarried,
                            loadBeingCarried = state.value.loadBeingCarried,
                            loadWeight = state.value.loadWeight,
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
        locationManager.onLocationPermissionGranted()
    }

    fun onLocationPermissionDenied() {
        locationManager.onLocationPermissionDenied()
    }

    fun onLocationSettingsDenied() {
        locationManager.onLocationSettingsDenied()
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepository.getWeatherByCoordinates(latitude, longitude)
                .onSuccess { weather ->
                    val weatherDescription = "${weather.description}, ${weather.temperature}°F"
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
        locationManager.clearError()
    }

    fun resetNavigation() {
        _navigateToDashboard.value = false
    }

} 