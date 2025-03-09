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
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.repository.vehicle.VehicleRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import app.forku.domain.repository.checklist.ChecklistRepository

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

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

    private var isSubmitting = false

    private var needsSync = false
    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private var hasLoadedChecks = false

    init {
        loadInitialData()
        checkLocationPermission()
        observeLocationUpdates()
        
        // Add observer for vehicleId changes
        viewModelScope.launch {
            _state.collect { state ->
                state.vehicleId?.let { vehicleId ->
                    loadVehiclePreShiftCheck(vehicleId)
                }
            }
        }
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

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // Load available vehicles first
                val vehicles = vehicleRepository.getVehicles()
                _state.update { it.copy(availableVehicles = vehicles) }

                // Then try to get current session
                val session = sessionRepository.getCurrentSession()
                val currentUser = authRepository.getCurrentUser()
                
                session?.vehicleId?.let { vehicleId ->
                    try {
                        val vehicle = vehicleRepository.getVehicle(vehicleId)
                        val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                        
                        _state.update { currentState ->
                            currentState.copy(
                                vehicleId = vehicle.id,
                                vehicleType = vehicle.type,
                                vehicleName = vehicle.codename,
                                sessionId = session.id,
                                operatorId = currentUser?.id,
                                lastPreshiftCheck = lastCheck?.lastCheckDateTime?.let { dateString ->
                                    LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                                },
                                preshiftCheckStatus = lastCheck?.status.toString()
                            )
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = "Failed to load vehicle details") }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load initial data") }
            }
        }
    }

    private fun loadVehiclePreShiftCheck(vehicleId: String) {
        if (hasLoadedChecks) return
        
        viewModelScope.launch {
            try {
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                _state.update { currentState -> 
                    currentState.copy(
                        lastPreshiftCheck = lastCheck?.lastCheckDateTime?.let { dateString ->
                            try {
                                LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                            } catch (e: Exception) {
                                null
                            }
                        },
                        preshiftCheckStatus = lastCheck?.status ?: "No preshift check recorded"
                    )
                }
                hasLoadedChecks = true
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load preshift check") }
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

    fun submitIncident() {
        if (isSubmitting) return
        
        viewModelScope.launch {
            try {
                isSubmitting = true
                
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
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to submit incident") }
            } finally {
                isSubmitting = false
            }
        }
    }

    fun resetForm() {
        hasLoadedChecks = false
        _state.update { IncidentReportState() }
    }

    fun dismissSuccessDialog() {
        _state.update { it.copy(showSuccessDialog = false) }
    }

    fun updateState(newState: IncidentReportState) {
        // If vehicle changed, trigger vehicle selection logic
        if (newState.vehicleId != state.value.vehicleId) {
            newState.vehicleId?.let { vehicleId ->
                state.value.availableVehicles.find { it.id == vehicleId }?.let { vehicle ->
                    onVehicleSelected(vehicle)
                    return
                }
            }
        }
        _state.update { newState }
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

    fun updateField(fieldName: String, value: String) {
        _state.update { currentState ->
            when (fieldName) {
                "location" -> currentState.copy(location = value)
                "locationDetails" -> currentState.copy(locationDetails = value)
                "weather" -> currentState.copy(weather = value)
                "injuries" -> currentState.copy(injuries = value)
                "vehicleName" -> currentState.copy(vehicleName = value)
                "loadBeingCarried" -> currentState.copy(loadBeingCarried = value)
                "description" -> currentState.copy(description = value)
                else -> currentState
            }
        }
        
        // Instead of immediate API call, just mark as needing sync
        needsSync = true
        
        // Debounce the actual API call
        debouncedSync()
    }

    private fun debouncedSync() {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(2000) // Increased debounce time to 2 seconds
            if (needsSync && shouldMakeApiCall()) {
                fetchUpdatedData()
                needsSync = false
            }
        }
    }

    private fun shouldMakeApiCall(): Boolean {
        val currentState = state.value
        return when (currentState.validate()) {
            is ValidationResult.Success -> !currentState.isLoading && currentState.type != null
            is ValidationResult.Error -> false
        }
    }

    private fun fetchUpdatedData() {
        viewModelScope.launch {
            try {
                // Your API call implementation
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        coroutineScope.cancel()
    }

    fun onVehicleSelected(vehicle: Vehicle) {
        hasLoadedChecks = false  // Reset flag when new vehicle is selected
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        vehicleId = vehicle.id,
                        vehicleType = vehicle.type,
                        vehicleName = vehicle.codename
                    )
                }
                loadVehiclePreShiftCheck(vehicle.id)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load vehicle details") }
            }
        }
    }

} 