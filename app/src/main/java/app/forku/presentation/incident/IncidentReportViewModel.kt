package app.forku.presentation.incident

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.incident.IncidentTypeEnum
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
import app.forku.domain.repository.checklist.ChecklistAnswerRepository

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import app.forku.domain.repository.user.UserRepository
import app.forku.presentation.navigation.Screen
import app.forku.domain.usecase.collision_incident.*
import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.model.incident.IncidentTypeFields
import java.time.Instant
import java.time.ZoneId
import app.forku.data.mapper.toCollisionIncidentDto
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log
import app.forku.data.mapper.toNearMissIncidentDto
import app.forku.data.repository.NearMissIncidentRepository
import app.forku.domain.usecase.nearmiss_incident.SaveNearMissIncidentUseCase
import app.forku.data.mapper.toHazardIncidentDto
import app.forku.domain.usecase.hazard_incident.SaveHazardIncidentUseCase
import app.forku.domain.usecase.vehiclefail_incident.SaveVehicleFailIncidentUseCase
import app.forku.data.mapper.toVehicleFailIncidentDto
import app.forku.data.api.FileUploaderApi

import app.forku.core.utils.toFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import app.forku.domain.usecase.incident.AddIncidentMultimediaUseCase
import app.forku.core.auth.HeaderManager
import app.forku.domain.usecase.gogroup.file.UploadFileUseCase
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.FileOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import app.forku.core.business.BusinessContextManager


@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val reportIncidentUseCase: ReportIncidentUseCase,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val checklistRepository: ChecklistRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val locationManager: LocationManager,
    private val notificationRepository: NotificationRepository,
    private val getCollisionIncidentByIdUseCase: GetCollisionIncidentByIdUseCase,
    private val saveCollisionIncidentUseCase: SaveCollisionIncidentUseCase,
    private val deleteCollisionIncidentUseCase: DeleteCollisionIncidentUseCase,
    private val saveNearMissIncidentUseCase: SaveNearMissIncidentUseCase,
    private val saveHazardIncidentUseCase: SaveHazardIncidentUseCase,
    private val saveVehicleFailIncidentUseCase: SaveVehicleFailIncidentUseCase,
    private val fileUploaderApi: FileUploaderApi,
    private val addIncidentMultimediaUseCase: AddIncidentMultimediaUseCase,
    private val headerManager: HeaderManager,
    private val uploadFileUseCase: UploadFileUseCase,
    private val businessContextManager: BusinessContextManager
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

    private val isSubmitting = AtomicBoolean(false)

    private var needsSync = false
    private var searchJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private var hasLoadedChecks = false

    data class UploadedPhoto(
        val uri: Uri,
        val internalName: String,
        val clientName: String,
        val fileSize: Int,
        val type: String
    )

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

                // Get business context from BusinessContextManager  
                val businessId = businessContextManager.getCurrentBusinessId()
                android.util.Log.d("IncidentReport", "Using businessId from BusinessContextManager: $businessId")
                
                // Load available vehicles first
                val vehicles = vehicleRepository.getVehicles(businessId ?: "")
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
                        val vehicle = vehicleRepository.getVehicle(vehicleId, businessId ?: "")
                        val lastCheck = checklistAnswerRepository.getLastChecklistAnswerForVehicle(vehicleId)

                        // Debug logs
                        android.util.Log.d("IncidentReportVM", "lastCheck: $lastCheck")
                        android.util.Log.d("IncidentReportVM", "lastCheck?.dateTime: ${lastCheck?.lastCheckDateTime}")
                        android.util.Log.d("IncidentReportVM", "lastCheck?.status: ${lastCheck?.status}")

                        _state.update { currentState ->
                            currentState.copy(
                                vehicleId = vehicle.id,
                                vehicleType = vehicle.type,
                                vehicleName = vehicle.codename,
                                sessionId = session.id,
                                lastPreshiftCheck = lastCheck?.lastCheckDateTime?.let {
                                    try {
                                        java.time.LocalDateTime.parse(it, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                                    } catch (e: Exception) {
                                        null
                                    }
                                },
                                preshiftCheckStatus = lastCheck?.status?.toString() ?: "No preshift check recorded",
                                checkId = lastCheck?.id
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("IncidentReportVM", "Error loading vehicle/checklist", e)
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
                // Get business context from BusinessContextManager
                val businessId = businessContextManager.getCurrentBusinessId()
                android.util.Log.d("IncidentReport", "[loadVehiclePreShiftCheck] Using businessId: $businessId")
                val lastCheck = checklistAnswerRepository.getLastChecklistAnswerForVehicle(vehicleId)
                _state.update { currentState -> 
                    currentState.copy(
                        lastPreshiftCheck = lastCheck?.lastCheckDateTime?.let {
                            try {
                                java.time.LocalDateTime.parse(it, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                            } catch (e: Exception) {
                                null
                            }
                        },
                        checkId = lastCheck?.id,
                        preshiftCheckStatus = lastCheck?.status?.toString() ?: "No preshift check recorded"
                    )
                }
                hasLoadedChecks = true
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load preshift checklist") }
            }
        }
    }

    fun setType(type: IncidentTypeEnum) {
        _state.update { it.copy(type = type) }
    }

    fun setIncidentType(incidentType: String) {
        try {
            val type = IncidentTypeEnum.valueOf(incidentType.uppercase().replace(" ", "_"))
            _state.update { it.copy(type = type) }
        } catch (e: IllegalArgumentException) {
            _state.update { it.copy(error = "Invalid incident type") }
        }
    }

    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun submitIncident() {
        Log.d("IncidentReportVM", "submitIncident called. isSubmitting=${isSubmitting.get()}")
        if (!isSubmitting.compareAndSet(false, true)) {
            Log.w("IncidentReportVM", "submitIncident: Already submitting, ignoring duplicate call.")
            return
        }
        viewModelScope.launch {
            try {
                _state.update { it.copy(attemptedSubmit = true) }
                
                // Log business context before submission
                val businessId = businessContextManager.getCurrentBusinessId()
                Log.d("IncidentReportVM", "=== INCIDENT SUBMISSION DEBUG ===")
                Log.d("IncidentReportVM", "BusinessId from BusinessContextManager: '$businessId'")
                Log.d("IncidentReportVM", "Current state userId: ${state.value.userId}")
                Log.d("IncidentReportVM", "Current state vehicleId: ${state.value.vehicleId}")
                Log.d("IncidentReportVM", "Current state sessionId: ${state.value.sessionId}")
                Log.d("IncidentReportVM", "Current state type: ${state.value.type}")
                Log.d("IncidentReportVM", "================================")
                
                when (val validationResult = state.value.validate()) {
                    is ValidationResult.Success -> {
                        _state.update { it.copy(isLoading = true) }
                        
                        try {
                            Log.d("IncidentReportVM", "Calling reportIncidentUseCase with validated data...")
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
                                othersInvolved = state.value.othersInvolved ?: "",
                                injuries = state.value.injuries,
                                injuryLocations = state.value.injuryLocations,
                                vehicleId = state.value.vehicleId,
                                vehicleType = state.value.vehicleType,
                                vehicleName = state.value.vehicleName,
                                isLoadCarried = state.value.isLoadCarried,
                                loadBeingCarried = state.value.loadBeingCarried,
                                loadWeight = state.value.loadWeightEnum,
                                photos = state.value.uploadedPhotos.map { it.uri },
                                locationCoordinates = state.value.locationCoordinates
                            )

                            result.onSuccess { incident ->
                                incident.id?.let { associatePhotosWithIncident(it) }
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
                isSubmitting.set(false)
                Log.d("IncidentReportVM", "submitIncident finished. isSubmitting reset to false")
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
        android.util.Log.d("IncidentReportVM", "retryFetchWeather called with lat=$latitude, lon=$longitude, retryCount=$retryCount")
        if (retryCount >= 3) {
            android.util.Log.w("IncidentReportVM", "Weather fetch failed after 3 retries. Setting weather to unavailable.")
            _state.update { it.copy(
                weather = "Weather data unavailable",
                weatherLoaded = true
            )}
            return
        }

        viewModelScope.launch {
            try {
                val weather = weatherRepository.getCurrentWeather(latitude, longitude)
                android.util.Log.d("IncidentReportVM", "weatherRepository.getCurrentWeather returned: '$weather'")
                if (weather.isNotBlank()) {
                    _state.update { it.copy(
                        weather = weather,
                        weatherLoaded = true
                    )}
                } else {
                    android.util.Log.w("IncidentReportVM", "Weather API returned blank. Retrying...")
                    delay(1000)
                    retryFetchWeather(latitude, longitude, retryCount + 1)
                }
            } catch (e: Exception) {
                android.util.Log.e("IncidentReportVM", "Exception fetching weather (attempt ${retryCount + 1})", e)
                if (retryCount < 2) {
                    delay(1000)
                    retryFetchWeather(latitude, longitude, retryCount + 1)
                } else {
                    _state.update { it.copy(
                        weather = "Weather data unavailable",
                        weatherLoaded = true,
                        error = null
                    )}
                }
            }
        }
    }

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            var tempConvertedFile: File? = null
            try {
                var file = uri.toFile(context)
                var mimeType = context.contentResolver.getType(uri) ?: "image/*"
                val ext = file.extension.lowercase()
                Log.d("IncidentReportVM", "[UPLOAD] File info: name=${file.name}, path=${file.absolutePath}, extension=${file.extension}, mimeType=$mimeType, size=${file.length()} bytes")

                // Si no es jpg/jpeg/png, convierte a jpg
                if (ext != "jpg" && ext != "jpeg" && ext != "png") {
                    Log.d("IncidentReportVM", "[UPLOAD] Converting image to JPG for compatibility...")
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        tempConvertedFile = File.createTempFile("upload_converted_", ".jpg", context.cacheDir)
                        FileOutputStream(tempConvertedFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        try {
                            val publicCopy = File("/sdcard/Download/${tempConvertedFile.name}")
                            tempConvertedFile.copyTo(publicCopy, overwrite = true)
                            Log.d("IncidentReportVM", "[UPLOAD] Copied converted file for inspection: ${publicCopy.absolutePath}, size=${publicCopy.length()} bytes")
                        } catch (copyEx: Exception) {
                            Log.e("IncidentReportVM", "[UPLOAD] Failed to copy converted file for inspection: ${copyEx.message}")
                        }
                        file = tempConvertedFile
                        mimeType = "application/octet-stream" // Usar este mimeType para archivos convertidos
                        Log.d("IncidentReportVM", "[UPLOAD] Conversion successful: ${file.name}, size=${file.length()} bytes, mimeType set to application/octet-stream")
                    } else {
                        Log.e("IncidentReportVM", "[UPLOAD] Failed to decode image for conversion.")
                        _state.update { it.copy(isLoading = false, error = "Failed to convert image for upload.") }
                        return@launch
                    }
                }

                val uploadResult = uploadFileUseCase.uploadFile(file, mimeType)
                uploadResult.onSuccess { uploadedFile ->
                    Log.d("IncidentReportVM", "[UPLOAD] Success: internalName=${uploadedFile.internalName}, clientName=${uploadedFile.clientName}, type=${uploadedFile.type}, fileSize=${uploadedFile.fileSize}")
                    val uploadedPhoto = UploadedPhoto(
                        uri = uri,
                        internalName = uploadedFile.internalName,
                        clientName = uploadedFile.clientName,
                        fileSize = uploadedFile.fileSize.toInt(),
                        type = uploadedFile.type
                    )
                    val backendUrl = "${app.forku.core.Constants.BASE_URL}api/multimedia/file/${uploadedFile.internalName}/Image"
                    Log.d("IncidentReportVM", "[UPLOAD] Expected backend image URL: $backendUrl")
                    _state.update { currentState ->
                        currentState.copy(
                            uploadedPhotos = currentState.uploadedPhotos + uploadedPhoto,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    Log.e("IncidentReportVM", "[UPLOAD] Failure: ${error.message}")
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to upload image") }
                }
            } catch (e: Exception) {
                Log.e("IncidentReportVM", "[UPLOAD] Exception: ${e.message}", e)
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to upload image") }
            } finally {
                // Borra el archivo temporal convertido si existe
                tempConvertedFile?.delete()
            }
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

    fun submitCollisionIncident() {
        Log.d("IncidentReportVM", "submitCollisionIncident called. isSubmitting=${isSubmitting.get()}")
        if (!isSubmitting.compareAndSet(false, true)) {
            Log.w("IncidentReportVM", "submitCollisionIncident: Already submitting, ignoring duplicate call.")
            return
        }
        viewModelScope.launch {
            try {
                _state.update { it.copy(attemptedSubmit = true) }
                when (val validationResult = state.value.validate()) {
                    is ValidationResult.Success -> {
                        _state.update { it.copy(isLoading = true) }
                        try {
                            Log.d("IncidentReportVM", "=== COLLISION INCIDENT SUBMISSION ===")
                            Log.d("IncidentReportVM", "Starting collision incident submission")
                            val businessId = businessContextManager.getCurrentBusinessId()
                            val siteId = businessContextManager.getCurrentSiteId()
                            val collisionDto = state.value.toCollisionIncidentDto(businessId)
                            Log.d("IncidentReportVM", "DTO created with businessId: '${collisionDto.businessId}'")
                            Log.d("IncidentReportVM", "DTO vehicle ID: '${collisionDto.vehicleId}'")
                            Log.d("IncidentReportVM", "DTO user ID: '${collisionDto.userId}'")
                            Log.d("IncidentReportVM", "Calling saveCollisionIncidentUseCase...")
                            saveCollisionIncidentUseCase(collisionDto, include = null, dateformat = "ISO8601").collect { result ->
                                result.onSuccess { collisionIncident ->
                                    Log.d("IncidentReportVM", "Collision incident saved successfully with ID: ${collisionIncident.id}")
                                    collisionIncident.id?.let { 
                                        Log.d("IncidentReportVM", "Triggering photo association for collision incident: $it")
                                        associatePhotosWithIncident(it) 
                                    }
                                    _state.update { it.copy(
                                        isLoading = false,
                                        showSuccessDialog = true
                                    ) }
                                }.onFailure { error ->
                                    Log.e("IncidentReportVM", "Failed to save collision incident", error)
                                    _state.update { it.copy(
                                        isLoading = false,
                                        error = error.message ?: "Failed to save collision details"
                                    ) }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("IncidentReportVM", "Exception during collision incident submission", e)
                            _state.update { it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to submit incident report"
                            ) }
                        }
                    }
                    is ValidationResult.Error -> {
                        Log.w("IncidentReportVM", "Validation failed: ${validationResult.message}")
                        _state.update { it.copy(error = validationResult.message) }
                    }
                }
            } catch (e: Exception) {
                Log.e("IncidentReportVM", "Unexpected error in submitCollisionIncident", e)
                _state.update { it.copy(error = "Failed to submit incident") }
            } finally {
                isSubmitting.set(false)
                Log.d("IncidentReportVM", "submitCollisionIncident finished. isSubmitting reset to false")
            }
        }
    }

    fun submitNearMissIncident() {
        Log.d("IncidentReportVM", "submitNearMissIncident called. isSubmitting=${isSubmitting.get()}")
        if (!isSubmitting.compareAndSet(false, true)) {
            Log.w("IncidentReportVM", "submitNearMissIncident: Already submitting, ignoring duplicate call.")
            return
        }
        viewModelScope.launch {
            try {
                _state.update { it.copy(attemptedSubmit = true) }
                when (val validationResult = state.value.validate()) {
                    is ValidationResult.Success -> {
                        _state.update { it.copy(isLoading = true) }
                        try {
                            Log.d("IncidentReportVM", "Starting near miss incident submission")
                            val nearMissDto = state.value.toNearMissIncidentDto()
                            saveNearMissIncidentUseCase(nearMissDto).collect { result ->
                                result.onSuccess { saved ->
                                    Log.d("IncidentReportVM", "Near miss incident saved successfully with ID: ${saved.id}")
                                    saved.id?.let { 
                                        Log.d("IncidentReportVM", "Triggering photo association for near miss incident: $it")
                                        associatePhotosWithIncident(it) 
                                    }
                                    _state.update { it.copy(
                                        isLoading = false,
                                        showSuccessDialog = true
                                    ) }
                                }.onFailure { error ->
                                    Log.e("IncidentReportVM", "Failed to save near miss incident", error)
                                    _state.update { it.copy(
                                        isLoading = false,
                                        error = error.message ?: "Failed to save near miss details"
                                    ) }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("IncidentReportVM", "Exception during near miss incident submission", e)
                            _state.update { it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to submit near miss incident"
                            ) }
                        }
                    }
                    is ValidationResult.Error -> {
                        Log.w("IncidentReportVM", "Validation failed: ${validationResult.message}")
                        _state.update { it.copy(error = validationResult.message) }
                    }
                }
            } catch (e: Exception) {
                Log.e("IncidentReportVM", "Unexpected error in submitNearMissIncident", e)
                _state.update { it.copy(error = "Failed to submit near miss incident") }
            } finally {
                isSubmitting.set(false)
                Log.d("IncidentReportVM", "submitNearMissIncident finished. isSubmitting reset to false")
            }
        }
    }

    fun submitHazardIncident() {
        Log.d("IncidentReportVM", "submitHazardIncident called. isSubmitting=${isSubmitting.get()}")
        if (!isSubmitting.compareAndSet(false, true)) {
            Log.w("IncidentReportVM", "submitHazardIncident: Already submitting, ignoring duplicate call.")
            return
        }
        viewModelScope.launch {
            try {
                _state.update { it.copy(attemptedSubmit = true) }
                when (val validationResult = state.value.validate()) {
                    is ValidationResult.Success -> {
                        _state.update { it.copy(isLoading = true) }
                        try {
                            Log.d("IncidentReportVM", "Starting hazard incident submission")
                            val hazardDto = state.value.toHazardIncidentDto()
                            saveHazardIncidentUseCase(hazardDto).collect { result ->
                                result.onSuccess { saved ->
                                    Log.d("IncidentReportVM", "Hazard incident saved successfully with ID: ${saved.id}")
                                    saved.id?.let { 
                                        Log.d("IncidentReportVM", "Triggering photo association for hazard incident: $it")
                                        associatePhotosWithIncident(it) 
                                    }
                                    _state.update { it.copy(
                                        isLoading = false,
                                        showSuccessDialog = true
                                    ) }
                                }.onFailure { error ->
                                    Log.e("IncidentReportVM", "Failed to save hazard incident", error)
                                    _state.update { it.copy(
                                        isLoading = false,
                                        error = error.message ?: "Failed to save hazard details"
                                    ) }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("IncidentReportVM", "Exception during hazard incident submission", e)
                            _state.update { it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to submit hazard report"
                            ) }
                        }
                    }
                    is ValidationResult.Error -> {
                        Log.w("IncidentReportVM", "Validation failed: ${validationResult.message}")
                        _state.update { it.copy(error = validationResult.message) }
                    }
                }
            } catch (e: Exception) {
                Log.e("IncidentReportVM", "Unexpected error in submitHazardIncident", e)
                _state.update { it.copy(error = "Failed to submit hazard incident") }
            } finally {
                isSubmitting.set(false)
                Log.d("IncidentReportVM", "submitHazardIncident finished. isSubmitting reset to false")
            }
        }
    }

    fun submitVehicleFailIncident() {
        Log.d("IncidentReportVM", "submitVehicleFailIncident called. isSubmitting=${isSubmitting.get()}")
        if (!isSubmitting.compareAndSet(false, true)) {
            Log.w("IncidentReportVM", "submitVehicleFailIncident: Already submitting, ignoring duplicate call.")
            return
        }
        viewModelScope.launch {
            try {
                _state.update { it.copy(attemptedSubmit = true) }
                when (val validationResult = state.value.validate()) {
                    is ValidationResult.Success -> {
                        _state.update { it.copy(isLoading = true) }
                        try {
                            Log.d("IncidentReportVM", "Starting vehicle fail incident submission")
                            val vehicleFailDto = state.value.toVehicleFailIncidentDto()
                            saveVehicleFailIncidentUseCase(vehicleFailDto).collect { result ->
                                result.onSuccess { saved ->
                                    Log.d("IncidentReportVM", "Vehicle fail incident saved successfully with ID: ${saved.id}")
                                    saved.id?.let { 
                                        Log.d("IncidentReportVM", "Triggering photo association for vehicle fail incident: $it")
                                        associatePhotosWithIncident(it) 
                                    }
                                    _state.update { it.copy(
                                        isLoading = false,
                                        showSuccessDialog = true
                                    ) }
                                }.onFailure { error ->
                                    Log.e("IncidentReportVM", "Failed to save vehicle fail incident", error)
                                    _state.update { it.copy(
                                        isLoading = false,
                                        error = error.message ?: "Failed to save vehicle fail details"
                                    ) }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("IncidentReportVM", "Exception during vehicle fail incident submission", e)
                            _state.update { it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to submit vehicle fail incident"
                            ) }
                        }
                    }
                    is ValidationResult.Error -> {
                        Log.w("IncidentReportVM", "Validation failed: ${validationResult.message}")
                        _state.update { it.copy(error = validationResult.message) }
                    }
                }
            } catch (e: Exception) {
                Log.e("IncidentReportVM", "Unexpected error in submitVehicleFailIncident", e)
                _state.update { it.copy(error = "Failed to submit vehicle fail incident") }
            } finally {
                isSubmitting.set(false)
                Log.d("IncidentReportVM", "submitVehicleFailIncident finished. isSubmitting reset to false")
            }
        }
    }

    // Asociar fotos con el incidente
    private fun associatePhotosWithIncident(incidentId: String) {
        Log.d("IncidentReportVM", "Starting photo association for incident: $incidentId")
        Log.d("IncidentReportVM", "Number of photos to associate: "+state.value.uploadedPhotos.size)
        val goUserId = currentUser.value?.id ?: run {
            Log.e("IncidentReportVM", "No current user found for photo association")
            return
        }
        state.value.uploadedPhotos.forEach { photo ->
            Log.d("IncidentReportVM", "\nProcessing photo:\n- Internal Name: ${photo.internalName}\n- Client Name: ${photo.clientName}\n- File Size: ${photo.fileSize}\n- Type: ${photo.type}")

            viewModelScope.launch {
                try {
                    val businessId = businessContextManager.getCurrentBusinessId()
                    val siteId = businessContextManager.getCurrentSiteId()
                    val creationDateTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    val entityMap = mapOf(
                        "GOUserId" to goUserId,
                        "IncidentId" to incidentId,
                        "MultimediaType" to 0,
                        "Image" to photo.internalName,
                        "ImageInternalName" to photo.internalName,
                        "ImageFileSize" to photo.fileSize,
                        "IsNew" to true,
                        "IsDirty" to true,
                        "IsMarkedForDeletion" to false,
                        "BusinessId" to businessId,
                        "SiteId" to siteId,
                        "CreationDateTime" to creationDateTime,
                        "EntityType" to 0
                    )
                    val entityJson = com.google.gson.Gson().toJson(entityMap)

                    Log.d("IncidentReportVM", "Created IncidentMultimediaDto JSON: $entityJson")

                    Log.d("IncidentReportVM", "Calling addIncidentMultimediaUseCase for photo: ${photo.internalName}")
                    val result = addIncidentMultimediaUseCase(entityJson)
                    result.onSuccess {
                        Log.d("IncidentReportVM", "Successfully associated multimedia: incidentId=$incidentId, internalName=${photo.internalName}")
                    }.onFailure { error ->
                        Log.e("IncidentReportVM", "Failed to associate multimedia: ${error.message}", error)
                    }
                } catch (e: Exception) {
                    Log.e("IncidentReportVM", "Exception while associating multimedia", e)
                }
            }
        }
        Log.d("IncidentReportVM", "Finished photo association process for incident: $incidentId")
    }

} 