package app.forku.presentation.incident

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.incident.IncidentType
import app.forku.domain.usecase.incident.ReportIncidentUseCase
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import app.forku.core.location.LocationManager
import app.forku.core.location.LocationState
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.repository.vehicle.VehicleRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.notification.NotificationRepository

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import app.forku.domain.repository.user.UserRepository
import app.forku.presentation.navigation.Screen


@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val checklistRepository: ChecklistRepository,
    private val locationManager: LocationManager,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentReportState())
    val state = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _navigateToDashboard = MutableStateFlow<String?>(null)
    val navigateToDashboard = _navigateToDashboard.asStateFlow()

    val locationState = locationManager.locationState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LocationState()
    )

    var tempPhotoUri: Uri? = null
        private set

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
                if (locationStateValue.location != null) {
                    _state.update { it.copy(
                        location = locationStateValue.location,
                        locationCoordinates = locationStateValue.location,
                        locationLoaded = true
                    )}
                }
                
                if (locationStateValue.latitude != null && locationStateValue.longitude != null) {
                    if (!state.value.weatherLoaded) {
                        retryFetchWeather(locationStateValue.latitude, locationStateValue.longitude)
                    }
                }

                locationStateValue.error?.let { error ->
                    _state.update { it.copy(error = error) }
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
                // Get current user first
                var user = userRepository.getCurrentUser()
                android.util.Log.d("IncidentReport", "Initial current user fetch: $user")
                
                // If no user found, try to refresh
                if (user == null) {
                    android.util.Log.d("IncidentReport", "No user found, attempting to refresh")
                    val refreshResult = userRepository.refreshCurrentUser()
                    user = refreshResult.getOrNull()
                    android.util.Log.d("IncidentReport", "After refresh, current user: $user")
                }

                val businessId = user?.businessId
                if (businessId == null) {
                    android.util.Log.e("IncidentReport", "No business context available")
                    _state.update { it.copy(error = "No business context available") }
                    return@launch
                }
                
                // Load available vehicles first
                val vehicles = vehicleRepository.getVehicles(businessId)
                _state.update { it.copy(availableVehicles = vehicles) }
                
                // Set user information regardless of session
                user?.let { currentUser ->
                    android.util.Log.d("IncidentReport", """
                        Setting user info:
                        - ID: ${currentUser.id}
                        - Name: ${currentUser.fullName}
                        - Token: ${currentUser.token.take(10)}...
                        - Role: ${currentUser.role}
                    """.trimIndent())
                    
                    _currentUser.value = currentUser
                    _state.update { currentState ->
                        currentState.copy(
                            userId = currentUser.id,
                            reporterName = currentUser.fullName
                        )
                    }
                } ?: run {
                    android.util.Log.e("IncidentReport", "No user found after refresh attempt")
                    _state.update { it.copy(error = "User not authenticated") }
                }

                // Then try to get current session
                val session = vehicleSessionRepository.getCurrentSession()
                android.util.Log.d("IncidentReport", "Current session: $session")
                
                session?.vehicleId?.let { vehicleId ->
                    try {
                        val vehicle = vehicleRepository.getVehicle(vehicleId, businessId)
                        val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId, businessId)
                        
                        _state.update { currentState ->
                            currentState.copy(
                                vehicleId = vehicle.id,
                                vehicleType = vehicle.type,
                                vehicleName = vehicle.codename,
                                sessionId = session.id,
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
                android.util.Log.e("IncidentReport", "Error in loadInitialData", e)
                _state.update { it.copy(error = "Failed to load initial data") }
            }
        }
    }

    private fun loadVehiclePreShiftCheck(vehicleId: String) {
        if (hasLoadedChecks) return
        
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId
                
                if (businessId == null) {
                    android.util.Log.e("IncidentReport", "No business context available")
                    _state.update { it.copy(error = "No business context available") }
                    return@launch
                }
                
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId, businessId)
                _state.update { currentState -> 
                    currentState.copy(
                        lastPreshiftCheck = lastCheck?.lastCheckDateTime?.let { dateString ->
                            try {
                                LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                            } catch (e: Exception) {
                                null
                            }
                        },
                        checkId = lastCheck?.id,
                        preshiftCheckStatus = lastCheck?.status ?: "No preshift check recorded"
                    )
                }
                hasLoadedChecks = true
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load preshift checklist") }
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
                _state.update { it.copy(attemptedSubmit = true) }
                
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
                                userId = state.value.userId,
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

                            result.onSuccess { incident ->
                                // Create a notification here
                                
                                _state.update { it.copy(
                                    isLoading = false,
                                    showSuccessDialog = true
                                ) }
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
        val route = when (_currentUser.value?.role) {
            UserRole.ADMIN -> Screen.AdminDashboard.route
            else -> Screen.Dashboard.route
        }
        _navigateToDashboard.value = route
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
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    locationLoaded = false,
                    weatherLoaded = false
                )}
                locationManager.startLocationUpdates()
                // Esperar un momento para que la ubicación se actualice
                delay(1000)
                // Forzar una actualización de ubicación
                locationManager.requestSingleUpdate()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error starting location updates: ${e.message}") }
            }
        }
    }

    fun onLocationPermissionDenied() {
        _state.update { it.copy(
            error = "Location permission is required to report incidents"
        )}
    }

    fun onLocationSettingsDenied() {
        _state.update { it.copy(
            error = "Location settings need to be enabled to report incidents"
        )}
    }

    private fun retryFetchWeather(latitude: Double, longitude: Double, retryCount: Int = 0) {
        if (retryCount >= 3) {
            _state.update { it.copy(
                weather = "Weather information unavailable",
                weatherLoaded = true
            )}
            return
        }

        viewModelScope.launch {
            try {
                val weather = weatherRepository.getCurrentWeather(latitude, longitude)
                if (weather.isNotBlank()) {
                    _state.update { it.copy(
                        weather = weather,
                        weatherLoaded = true
                    )}
                } else {
                    delay(1000) // Wait 1 second before retry
                    retryFetchWeather(latitude, longitude, retryCount + 1)
                }
            } catch (e: Exception) {
                android.util.Log.e("Weather", "Error fetching weather (attempt ${retryCount + 1})", e)
                if (retryCount < 2) {
                    delay(1000) // Wait 1 second before retry
                    retryFetchWeather(latitude, longitude, retryCount + 1)
                } else {
                    _state.update { it.copy(
                        weather = "Weather information unavailable",
                        weatherLoaded = true,
                        error = null // Don't show error to user, just set default weather
                    )}
                }
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
        _navigateToDashboard.value = null
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