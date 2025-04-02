package app.forku.presentation.checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.RotationRules
import app.forku.domain.usecase.checklist.GetChecklistUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.usecase.checklist.SubmitChecklistUseCase
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.core.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val getChecklistUseCase: GetChecklistUseCase,
    private val submitChecklistUseCase: SubmitChecklistUseCase,
    private val validateChecklistUseCase: ValidateChecklistUseCase,
    private val userRepository: UserRepository,
    private val checklistRepository: ChecklistRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val locationManager: LocationManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId = checkNotNull(savedStateHandle["vehicleId"])
    
    private val _state = MutableStateFlow<ChecklistState?>(null)
    val state = _state.asStateFlow()

    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog = _showDiscardDialog.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            // Start location updates
            locationManager.startLocationUpdates()
            
            // Observe location state
            locationManager.locationState.collectLatest { locationState ->
                locationState.error?.let { error ->
                    _state.update { it?.copy(
                        showErrorModal = true,
                        errorModalMessage = "Error de ubicación: $error"
                    )}
                }
            }

            // Prevent creating new checks if there's an active session
            val currentSession = vehicleSessionRepository.getCurrentSession()
            if (currentSession != null) {
                // Get the last completed check for this vehicle
                val lastCompletedCheck = checklistRepository.getLastPreShiftCheck(vehicleId.toString())
                if (lastCompletedCheck != null) {
                    loadExistingCheck(lastCompletedCheck.id)
                } else {
                    loadChecklistData() // Fallback to normal flow if no check found
                }
            } else {
                loadChecklistData()
            }
            startTimer()
        }
    }

    // Add location permission handlers
    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            try {
                locationManager.startLocationUpdates()
                // Force a single location update
                locationManager.requestSingleUpdate()
            } catch (e: Exception) {
                _state.update { it?.copy(
                    showErrorModal = true,
                    errorModalMessage = "Error al iniciar la ubicación: ${e.message}"
                )}
            }
        }
    }

    fun onLocationPermissionDenied() {
        _state.update { it?.copy(
            showErrorModal = true,
            errorModalMessage = "Se requiere el permiso de ubicación para completar el checklist"
        )}
    }

    fun onLocationSettingsDenied() {
        _state.update { it?.copy(
            showErrorModal = true,
            errorModalMessage = "Se requiere activar la ubicación para completar el checklist"
        )}
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Update every second
                _state.value?.let { currentState ->
                    if (!currentState.isCompleted && currentState.startDateTime != null) {
                        try {
                            val startDateTime = currentState.startDateTime
                            android.util.Log.d("ChecklistViewModel", "Timer - Current startDateTime: $startDateTime")
                            
                            // Parse the date using ZonedDateTime first, then convert to Instant
                            val startTimeMillis = try {
                                val zonedDateTime = java.time.ZonedDateTime.parse(startDateTime)
                                zonedDateTime.toInstant().toEpochMilli()
                            } catch (e: Exception) {
                                android.util.Log.e("ChecklistViewModel", "Failed to parse date: $startDateTime", e)
                                // Try parsing as Instant directly if ZonedDateTime fails
                                try {
                                    val instantStr = startDateTime.substringBefore("[")
                                    java.time.Instant.parse(instantStr).toEpochMilli()
                                } catch (e2: Exception) {
                                    android.util.Log.e("ChecklistViewModel", "Failed to parse as Instant: $startDateTime", e2)
                                    System.currentTimeMillis() // Use current time as fallback
                                }
                            }
                            
                            val newElapsedTime = System.currentTimeMillis() - startTimeMillis
                            android.util.Log.d("ChecklistViewModel", "Timer - Start time millis: $startTimeMillis")
                            android.util.Log.d("ChecklistViewModel", "Timer - Current time millis: ${System.currentTimeMillis()}")
                            android.util.Log.d("ChecklistViewModel", "Timer - Calculated elapsed time: $newElapsedTime ms")
                            
                            _state.update { state -> 
                                state?.copy(elapsedTime = newElapsedTime.coerceAtLeast(0))
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ChecklistViewModel", "Error updating timer", e)
                        }
                    } else {
                        android.util.Log.d("ChecklistViewModel", "Timer - Check completed or no start time available")
                        android.util.Log.d("ChecklistViewModel", "Timer - isCompleted: ${currentState.isCompleted}")
                        android.util.Log.d("ChecklistViewModel", "Timer - startDateTime: ${currentState.startDateTime}")
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun loadChecklistData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Obtener datos del checklist
                val checklists = getChecklistUseCase(vehicleId.toString())
                val firstChecklist = checklists.first()
                val allItems = checklists.flatMap { it.items }
                val selectedItems = selectQuestionsForRotation(
                    allItems,
                    firstChecklist.rotationRules
                )

                // 2. Crear o recuperar el check
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId.toString())
                
                // Get current time in ISO format
                val currentDateTime = java.time.Instant.now().toString()
                android.util.Log.d("ChecklistViewModel", "Setting initial startDateTime: $currentDateTime")

                val checkId = if (lastCheck?.status == CheckStatus.IN_PROGRESS.toString()) {
                    lastCheck.id
                } else {
                    // Get current location before creating new check
                    val locationState = locationManager.locationState.value
                    android.util.Log.d("ChecklistViewModel", "Location state during check creation: $locationState")
                    
                    // Request a single location update if we don't have location
                    if (locationState.location == null) {
                        android.util.Log.w("ChecklistViewModel", "Location is null during check creation, requesting update")
                        locationManager.requestSingleUpdate()
                    }

                    val initialCheck = submitChecklistUseCase(
                        vehicleId = vehicleId.toString(),
                        items = selectedItems,
                        location = locationState.location,
                        locationCoordinates = locationState.location
                    )
                    initialCheck.id
                }

                // 3. Obtener datos del vehículo
                val vehicle = getVehicleUseCase(vehicleId.toString())

                withContext(Dispatchers.Main) {
                    _state.value = ChecklistState(
                        vehicle = vehicle,
                        vehicleId = vehicleId.toString(),
                        vehicleStatus = VehicleStatus.AVAILABLE,
                        checkItems = if (lastCheck?.status == CheckStatus.IN_PROGRESS.toString())
                            lastCheck.items else selectedItems,
                        rotationRules = firstChecklist.rotationRules,
                        checkId = checkId,
                        checkStatus = CheckStatus.IN_PROGRESS.toString(),
                        startDateTime = lastCheck?.startDateTime ?: currentDateTime
                    )
                    
                    // Start timer after state is set
                    startTimer()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = ChecklistState(
                        vehicleId = vehicleId.toString(),
                        vehicleStatus = VehicleStatus.AVAILABLE,
                        checkStatus = CheckStatus.NOT_STARTED.toString(),
                        error = "Failed to load checklist: ${e.message}"
                    )
                }
            }
        }
    }

    private fun selectQuestionsForRotation(
        allItems: List<ChecklistItem>,
        rules: RotationRules
    ): List<ChecklistItem> {
        val selectedQuestions = mutableListOf<ChecklistItem>()
        
        // Get critical questions
        val criticalQuestions = allItems
            .filter { it.isCritical }
            .shuffled()
            .take(rules.criticalQuestionMinimum)
        selectedQuestions.addAll(criticalQuestions)

        // Get required category questions
        rules.requiredCategories.forEach { categoryName ->
            val categoryQuestions = allItems
                .filter { 
                    it.category == categoryName &&
                    !selectedQuestions.contains(it) 
                }
                .shuffled()
                .take(1)
            selectedQuestions.addAll(categoryQuestions)
        }

        // Fill remaining slots with standard questions
        val remainingSlots = rules.maxQuestionsPerCheck - selectedQuestions.size
        if (remainingSlots > 0) {
            val standardQuestions = allItems
                .filter { 
                    !it.isCritical && 
                    !selectedQuestions.contains(it) 
                }
                .shuffled()
                .take(minOf(remainingSlots, rules.standardQuestionMaximum))
            selectedQuestions.addAll(standardQuestions)
        }

        return selectedQuestions.shuffled()
    }

    fun updateItemResponse(id: String, isYes: Boolean) {
        if (state.value?.isReadOnly == true) {
            return // Don't allow updates if check is read-only
        }
        viewModelScope.launch {
            try {
                val currentItems = state.value?.checkItems?.toMutableList() ?: mutableListOf()
                val itemIndex = currentItems.indexOfFirst { it.id == id }
                
                if (itemIndex != -1) {
                    val item = currentItems[itemIndex]
                    val newAnswer = if (isYes) Answer.PASS else Answer.FAIL
                    currentItems[itemIndex] = item.copy(userAnswer = newAnswer)
                    
                    // Update local state first for immediate UI feedback
                    val validation = validateChecklistUseCase(currentItems)
                    _state.update { currentState ->
                        currentState?.copy(
                            checkItems = currentItems,
                            isCompleted = validation.isComplete,
                            vehicleBlocked = validation.isBlocked
                        )
                    }

                    // Submit to API with current timestamp
                    try {
                        // Get current location
                        val locationState = locationManager.locationState.value
                        android.util.Log.d("ChecklistViewModel", "Location state during update: $locationState")
                        android.util.Log.d("ChecklistViewModel", "Location string: ${locationState.location}")
                        android.util.Log.d("ChecklistViewModel", "Latitude: ${locationState.latitude}")
                        android.util.Log.d("ChecklistViewModel", "Longitude: ${locationState.longitude}")
                        
                        // Verify we have location before proceeding
                        if (locationState.location == null) {
                            android.util.Log.w("ChecklistViewModel", "Location is null during update")
                            // Request a single location update
                            locationManager.requestSingleUpdate()
                            // Don't block the UI, continue with null location
                        }

                        // Use the location string directly from the state
                        val locationCoordinates = locationState.location

                        android.util.Log.d("ChecklistViewModel", "Submitting update with location coordinates: $locationCoordinates")

                        checklistRepository.submitPreShiftCheck(
                            vehicleId = state.value?.vehicleId ?: "",
                            checkItems = currentItems,
                            checkId = state.value?.checkId ?: "",
                            locationCoordinates = locationCoordinates
                        )
                    } catch (e: Exception) {
                        // Log error but don't disrupt user experience
                        android.util.Log.e("Checklist", "Failed to sync answer", e)
                        _state.update { it?.copy(
                            errorModalMessage = "Changes saved locally but failed to sync: ${e.message}",
                            showErrorModal = true
                        ) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it?.copy(
                    error = "Failed to update answer: ${e.message}",
                    showErrorModal = true
                ) }
            }
        }
    }

    fun clearItemResponse(id: String) {
        if (state.value?.isReadOnly == true) {
            return // Don't allow updates if check is read-only
        }
        val currentItems = state.value?.checkItems?.toMutableList() ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.id == id }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(userAnswer = null)
            _state.update { 
                it?.copy(
                    checkItems = currentItems,
                    isCompleted = false,
                    vehicleBlocked = false
                )
            }
        }
    }

    private suspend fun isUserAuthenticated(): Boolean {
        return userRepository.getCurrentUser() != null
    }

    fun hasUnsavedChanges(): Boolean {
        return state.value?.let { currentState ->
            currentState.checkItems.any { it.userAnswer != null } && !currentState.isSubmitted
        } ?: false
    }

    fun onBackPressed() {
        if (hasUnsavedChanges()) {
            _showDiscardDialog.value = true
        } else {
            _navigationEvent.value = NavigationEvent.Back
        }
    }

    fun onDiscardConfirmed() {
        _showDiscardDialog.value = false
        // Cancel any ongoing operations
        timerJob?.cancel()
        _navigationEvent.value = NavigationEvent.Back
    }

    fun onDiscardDismissed() {
        _showDiscardDialog.value = false
    }

    fun resetNavigation() {
        _navigationEvent.value = null
    }

    fun submitCheck() {
        if (state.value?.isReadOnly == true) {
            return // Don't allow submission if check is read-only
        }
        viewModelScope.launch {
            try {
                _state.update { it?.copy(isSubmitting = true) }
                
                val currentItems = state.value?.checkItems ?: mutableListOf()
                val validation = validateChecklistUseCase(items = currentItems)
                
                if (!validation.isComplete) {
                    _state.update { 
                        it?.copy(
                            showErrorModal = true,
                            errorModalMessage = "Por favor completa todas las preguntas requeridas antes de finalizar",
                            isSubmitting = false
                        )
                    }
                    return@launch
                }

                // Get current location
                val locationState = locationManager.locationState.value
                android.util.Log.d("ChecklistViewModel", "Location state: $locationState")
                
                // Verify we have location before proceeding
                if (locationState.location == null) {
                    android.util.Log.w("ChecklistViewModel", "Location is null, cannot proceed with submission")
                    _state.update { 
                        it?.copy(
                            showErrorModal = true,
                            errorModalMessage = "Se requiere la ubicación para enviar el checklist. Por favor verifica que la ubicación esté activada.",
                            isSubmitting = false
                        )
                    }
                    return@launch
                }

                val locationCoordinates = if (locationState.latitude != null && locationState.longitude != null) {
                    "${locationState.latitude},${locationState.longitude}"
                } else null

                android.util.Log.d("ChecklistViewModel", "Location coordinates: $locationCoordinates")
                android.util.Log.d("ChecklistViewModel", "Location state latitude: ${locationState.latitude}")
                android.util.Log.d("ChecklistViewModel", "Location state longitude: ${locationState.longitude}")

                // Determine final status based on validation and completion
                val updatedCheck = submitChecklistUseCase(
                    vehicleId = vehicleId.toString(),
                    items = currentItems,
                    checkId = state.value?.checkId ?: "",
                    status = validation.status.name,
                    location = locationState.location,
                    locationCoordinates = locationCoordinates
                )

                // If checklist passed, start the session
                if (validation.canStartSession) {
                    try {
                        vehicleSessionRepository.startSession(
                            vehicleId = vehicleId.toString(),
                            checkId = updatedCheck.id
                        )
                    } catch (e: Exception) {
                        _state.update {
                            it?.copy(
                                showErrorModal = true,
                                errorModalMessage = "Check completado pero no se pudo iniciar sesión: ${e.message}"
                            )
                        }
                    }
                }

                // Get current user to determine navigation
                val currentUser = userRepository.getCurrentUser()
                val isAdmin = currentUser?.role == UserRole.ADMIN

                _state.update { 
                    it?.copy(
                        isSubmitting = false,
                        isCompleted = true,
                        checkItems = updatedCheck.items,
                        checkStatus = updatedCheck.status,
                        isSubmitted = true
                    )
                }

                // Trigger navigation based on user role
                _navigationEvent.value = NavigationEvent.AfterSubmit(isAdmin)

            } catch (e: Exception) {
                _state.update {
                    it?.copy(
                        isSubmitting = false,
                        showErrorModal = true,
                        errorModalMessage = "Error al guardar el check: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadExistingCheck(checkId: String) {
        viewModelScope.launch {
            try {
                val check = checklistRepository.getCheckById(checkId)
                val vehicle = getVehicleUseCase(vehicleId.toString())
                
                check?.let {
                    _state.value = ChecklistState(
                        vehicle = vehicle,
                        vehicleId = vehicleId.toString(),
                        vehicleStatus = VehicleStatus.IN_USE,
                        checkItems = it.items,
                        checkId = it.id,
                        checkStatus = it.status,
                        isCompleted = it.status != CheckStatus.IN_PROGRESS.toString(),
                        isSubmitted = it.status != CheckStatus.IN_PROGRESS.toString(),
                        isReadOnly = it.status != CheckStatus.IN_PROGRESS.toString(),
                        startDateTime = it.startDateTime
                    )
                }
            } catch (e: Exception) {
                _state.value = ChecklistState(
                    vehicleId = vehicleId.toString(),
                    vehicleStatus = VehicleStatus.AVAILABLE,
                    checkStatus = CheckStatus.NOT_STARTED.toString(),
                    error = "Failed to load existing check: ${e.message}"
                )
            }
        }
    }
}

sealed class NavigationEvent {
    object Back : NavigationEvent()
    data class AfterSubmit(val isAdmin: Boolean) : NavigationEvent()
}