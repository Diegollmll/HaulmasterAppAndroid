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
import app.forku.domain.repository.checklist.ChecklistItemCategoryRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.usecase.session.StartVehicleSessionUseCase
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import android.net.Uri
import app.forku.domain.usecase.gogroup.file.UploadFileUseCase
import app.forku.domain.usecase.checklist.AddChecklistItemAnswerMultimediaUseCase
import app.forku.domain.usecase.checklist.DeleteChecklistItemAnswerMultimediaUseCase
import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto
import app.forku.data.api.dto.multimedia.MultimediaDto
import java.io.File
import android.content.Context
import android.provider.OpenableColumns
import com.google.gson.Gson
import app.forku.domain.usecase.checklist.GetChecklistItemAnswerMultimediaByAnswerIdUseCase
import app.forku.core.auth.HeaderManager
import kotlinx.coroutines.flow.map
import app.forku.domain.usecase.safetyalert.CreateSafetyAlertUseCase
import android.content.ContentProvider
import android.content.ContentResolver
import androidx.core.content.FileProvider
import android.os.Build
import android.provider.MediaStore
import android.content.ContentValues
import app.forku.core.business.BusinessContextManager
import app.forku.domain.usecase.certification.ValidateUserCertificationUseCase

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
    private val checklistItemCategoryRepository: ChecklistItemCategoryRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    private val answeredChecklistItemRepository: AnsweredChecklistItemRepository,
    private val startVehicleSessionUseCase: StartVehicleSessionUseCase,
    private val vehicleStatusUpdater: VehicleStatusUpdater,
    savedStateHandle: SavedStateHandle,
    private val uploadFileUseCase: UploadFileUseCase,
    private val addChecklistItemAnswerMultimediaUseCase: AddChecklistItemAnswerMultimediaUseCase,
    private val deleteChecklistItemAnswerMultimediaUseCase: DeleteChecklistItemAnswerMultimediaUseCase,
    private val appContext: Context,
    private val gson: Gson,
    private val getChecklistItemAnswerMultimediaByAnswerIdUseCase: GetChecklistItemAnswerMultimediaByAnswerIdUseCase,
    private val headerManager: HeaderManager,
    private val createSafetyAlertUseCase: CreateSafetyAlertUseCase,
    private val businessContextManager: BusinessContextManager,
    private val validateUserCertificationUseCase: ValidateUserCertificationUseCase
) : ViewModel() {

    private val vehicleId = checkNotNull(savedStateHandle["vehicleId"])
    
    // Initialize state without SavedStateHandle
    private val _state = MutableStateFlow<ChecklistState?>(null)
    val state = _state.asStateFlow()

    private val _showDiscardDialog = MutableStateFlow(false)
    val showDiscardDialog = _showDiscardDialog.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    // Save only serializable data
    private val _categoryNameMap = MutableStateFlow<Map<String, String>>(
        savedStateHandle.get<String>("categoryNameMapJson")?.let {
            try {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                gson.fromJson(it, type) as Map<String, String>
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error parsing categoryNameMap: ${e.message}")
                emptyMap()
            }
        } ?: emptyMap()
    )
    val categoryNameMap = _categoryNameMap.asStateFlow()

    // Save only serializable data for item images
    private val _itemImages = MutableStateFlow<Map<String, List<String>>>(
        savedStateHandle.get<String>("itemImagesJson")?.let {
            try {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, List<String>>>() {}.type
                gson.fromJson(it, type) as Map<String, List<String>>
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error parsing itemImages: ${e.message}")
                emptyMap()
            }
        } ?: emptyMap()
    )
    val itemImages = _itemImages.map { map ->
        map.mapValues { (_, uris) -> uris.map { Uri.parse(it) } }
    }

    // Save only serializable data for answered item IDs
    private val _answeredItemIds = MutableStateFlow<Map<String, String>>(
        savedStateHandle.get<String>("answeredItemIdsJson")?.let {
            try {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                gson.fromJson(it, type) as Map<String, String>
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error parsing answeredItemIds: ${e.message}")
                emptyMap()
            }
        } ?: emptyMap()
    )
    val answeredItemIds = _answeredItemIds.asStateFlow()

    // Save only serializable data for uploaded multimedia
    private val _uploadedMultimedia = MutableStateFlow<Map<String, List<ChecklistItemAnswerMultimediaDto>>>(
        savedStateHandle.get<String>("uploadedMultimediaJson")?.let {
            try {
                val type = object : com.google.gson.reflect.TypeToken<Map<String, List<ChecklistItemAnswerMultimediaDto>>>() {}.type
                gson.fromJson(it, type) as Map<String, List<ChecklistItemAnswerMultimediaDto>>
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error parsing uploadedMultimedia: ${e.message}")
                emptyMap()
            }
        } ?: emptyMap()
    )
    val uploadedMultimedia = _uploadedMultimedia.asStateFlow()

    private val _uploading = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val uploading = _uploading.asStateFlow()
    private val _uploadErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val uploadErrors = _uploadErrors.asStateFlow()
    private val _uploadingImages = MutableStateFlow<Set<Uri>>(emptySet())
    val uploadingImages = _uploadingImages.asStateFlow()

    private var _tempPhotoUri = MutableStateFlow<Uri?>(null)
    val tempPhotoUri: Uri? get() = _tempPhotoUri.value

    companion object {
        private const val TAG = "ChecklistUserComment"
    }

    init {
        android.util.Log.e("ChecklistViewModel", "INIT ChecklistViewModel - ViewModel is being created/recreated!")
        android.util.Log.d("ChecklistViewModel", "INIT - Estado actual: ${_state.value}")
        android.util.Log.d("ChecklistViewModel", "INIT - answeredItemIds: ${_answeredItemIds.value}")
        android.util.Log.d("ChecklistViewModel", "INIT - uploadedMultimedia: ${_uploadedMultimedia.value}")
        viewModelScope.launch {
            // Start location updates
            locationManager.startLocationUpdates()
            
            // Observe location state
            locationManager.locationState.collectLatest { locationState ->
                locationState.error?.let { error ->
                    _state.update { it?.copy(
                        showErrorModal = true,
                        errorModalMessage = "Location error: $error"
                    )}
                }
            }

            // Prevent creating new checks if there's an active session
            val currentSession = vehicleSessionRepository.getCurrentSession()
            android.util.Log.d("ChecklistViewModel", "INIT - Current session: $currentSession")
            
            if (currentSession != null) {
                // Use BusinessContextManager for business context
                val businessId = businessContextManager.getCurrentBusinessId()
                android.util.Log.d("ChecklistViewModel", "INIT - businessId from BusinessContextManager: $businessId")
                
                val lastCompletedCheck = checklistRepository.getLastPreShiftCheck(vehicleId.toString(), businessId ?: "")
                android.util.Log.d("ChecklistViewModel", "INIT - Last completed check: $lastCompletedCheck")
                if (lastCompletedCheck != null) {
                    android.util.Log.d("ChecklistViewModel", "INIT - (lastCompletedCheck != null) AA")
                    loadExistingCheck(lastCompletedCheck.id)
                    android.util.Log.d("ChecklistViewModel", "INIT - (lastCompletedCheck != null) VV")
                } else {
                    android.util.Log.d("ChecklistViewModel", "INIT - (lastCompletedCheck != null) QQ")
                    loadChecklistData() // Fallback to normal flow if no check found
                    android.util.Log.d("ChecklistViewModel", "INIT - (lastCompletedCheck != null) ZZ")
                }
            } else {
                loadChecklistData()
            }
            startTimer()
        }

//        viewModelScope.launch {
//            _state.collect { state ->
//                savedStateHandle["state"] = state
//            }
//        }
//
        viewModelScope.launch {
            _categoryNameMap.collect { map ->
                savedStateHandle["categoryNameMapJson"] = gson.toJson(map)
            }
        }
        viewModelScope.launch {
            _itemImages.collect { images ->
                // Convert Uri to String before saving
                val serializableMap = images.mapValues { (_, uris) -> uris.map { it.toString() } }
                savedStateHandle["itemImagesJson"] = gson.toJson(serializableMap)
            }
        }
        viewModelScope.launch {
            _answeredItemIds.collect { ids ->
                savedStateHandle["answeredItemIdsJson"] = gson.toJson(ids)
            }
        }
        viewModelScope.launch {
            _uploadedMultimedia.collect { multimedia ->
                savedStateHandle["uploadedMultimediaJson"] = gson.toJson(multimedia)
            }
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
                    errorModalMessage = "Error starting location: ${e.message}"
                )}
            }
        }
    }

    fun onLocationPermissionDenied() {
        _state.update { it?.copy(
            showErrorModal = true,
            errorModalMessage = "Location permission is required to complete the checklist"
        )}
    }

    fun onLocationSettingsDenied() {
        _state.update { it?.copy(
            showErrorModal = true,
            errorModalMessage = "Location must be enabled to complete the checklist"
        )}
    }

    private fun startTimer() {
        android.util.Log.d("ChecklistViewModel", "startTimer() called A")
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            android.util.Log.d("ChecklistViewModel", "startTimer() called B")
            while (true) {
                android.util.Log.d("ChecklistViewModel", "startTimer() called C")
                delay(1000)
                android.util.Log.d("ChecklistViewModel", "startTimer() called D")
                _state.value?.let { currentState ->
                    if (currentState.isCompleted) {
                        android.util.Log.d("ChecklistViewModel", "startTimer Timer stopped: checklist is completed.")
                        timerJob?.cancel()
                        timerJob = null
                        return@launch
                    }
                    // --- Remove or comment out timer debug logs ---
                    android.util.Log.d("ChecklistViewModel", "startTimer [TIMER] Loop: startDateTime=${currentState.startDateTime}, isCompleted=${currentState.isCompleted}, elapsedTime=${currentState.elapsedTime}")
                    if (!currentState.isCompleted && currentState.startDateTime != null) {
                        try {
                            android.util.Log.d("ChecklistViewModel", "startTimer() called D")
                            val startDateTime = currentState.startDateTime
                            android.util.Log.d("ChecklistViewModel", "startTimer() called D")
                            android.util.Log.d("ChecklistViewModel", "[TIMER] Parsing startDateTime: $startDateTime")

                            val startTimeMillis = try {
                                android.util.Log.d("ChecklistViewModel", "startTimer() called D")
                                val zonedDateTime = java.time.ZonedDateTime.parse(startDateTime)
                                zonedDateTime.toInstant().toEpochMilli()
                            } catch (e: Exception) {
                                android.util.Log.e("ChecklistViewModel", "startTimer [TIMER] Failed to parse as ZonedDateTime: $startDateTime", e)
                                try {
                                    val instantStr = startDateTime.substringBefore("[")
                                    java.time.Instant.parse(instantStr).toEpochMilli()
                                } catch (e2: Exception) {
                                    // android.util.Log.e("ChecklistViewModel", "[TIMER] Failed to parse as Instant: $startDateTime", e2)
                                    System.currentTimeMillis()
                                }
                            }
                            val newElapsedTime = System.currentTimeMillis() - startTimeMillis
                            // android.util.Log.d("ChecklistViewModel", "[TIMER] startTimeMillis=$startTimeMillis, now=${System.currentTimeMillis()}, newElapsedTime=$newElapsedTime")
                            _state.update { state ->
                                // android.util.Log.d("ChecklistViewModel", "[TIMER] Updating state with elapsedTime=${newElapsedTime.coerceAtLeast(0)}")
                                state?.copy(elapsedTime = newElapsedTime.coerceAtLeast(0))
                            }
                        } catch (e: Exception) {
                            // android.util.Log.e("ChecklistViewModel", "[TIMER] Error updating timer", e)
                        }
                    } else {
                        // android.util.Log.d("ChecklistViewModel", "[TIMER] Timer not running: isCompleted=${currentState.isCompleted}, startDateTime=${currentState.startDateTime}")
                    }
                }
            }
        }
    }

    override fun onCleared() {
        android.util.Log.d("ChecklistViewModel", "onCleared - ViewModel is being cleared")
        android.util.Log.d("ChecklistViewModel", "onCleared - Final state: ${_state.value}")
        android.util.Log.d("ChecklistViewModel", "onCleared - Final answeredItemIds: ${_answeredItemIds.value}")
        android.util.Log.d("ChecklistViewModel", "onCleared - Final uploadedMultimedia: ${_uploadedMultimedia.value}")
        super.onCleared()
        timerJob?.cancel()
    }

    private fun maybeStartTimer() {
        android.util.Log.d("ChecklistViewModel", "maybeStartTimer A")
        val s = _state.value
        android.util.Log.d("ChecklistViewModel", "maybeStartTimer B")
        if (s?.startDateTime != null && s.isCompleted == false) {
            android.util.Log.d("ChecklistViewModel", "maybeStartTimer C")
            startTimer()
            android.util.Log.d("ChecklistViewModel", "maybeStartTimer D")
        }
    }

    fun loadChecklistData() {
        android.util.Log.d("ChecklistViewModel", "loadChecklistData === 🚗 STARTING CHECKLIST DATA LOAD ===")
        android.util.Log.d("ChecklistViewModel", "loadChecklistData vehicleId: $vehicleId")
        android.util.Log.d("ChecklistViewModel", "loadChecklistData - Estado actual: ${_state.value}")
        android.util.Log.d("ChecklistViewModel", "loadChecklistData - answeredItemIds: ${_answeredItemIds.value}")
        android.util.Log.d("ChecklistViewModel", "loadChecklistData - uploadedMultimedia: ${_uploadedMultimedia.value}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("ChecklistViewModel", "🔍 Iniciando carga de checklist para vehicleId=$vehicleId")
                
                // Use BusinessContextManager for business context
                val businessId = businessContextManager.getCurrentBusinessId()
                android.util.Log.d("ChecklistViewModel", "businessId from BusinessContextManager: $businessId")
                
                // Business ID should always come from BusinessContextManager

                // Fetch all categories and build the map
                val categories = checklistItemCategoryRepository.getAllCategories()
                val categoryMap = categories.associate { it.id to it.name }
                android.util.Log.d("ChecklistViewModel", "Category map: $categoryMap")
                _categoryNameMap.value = categoryMap

                // Fetch vehicle for summary display and type filtering
                android.util.Log.d("ChecklistViewModel", "🚗 Llamando a getVehicleUseCase con vehicleId=$vehicleId")
                val vehicle = try {
                    getVehicleUseCase(vehicleId.toString()).also { fetchedVehicle ->
                        android.util.Log.d("ChecklistViewModel", "🚗 Vehicle fetched successfully:")
                        android.util.Log.d("ChecklistViewModel", "  - ID: ${fetchedVehicle?.id}")
                        android.util.Log.d("ChecklistViewModel", "  - Codename: ${fetchedVehicle?.codename}")
                        android.util.Log.d("ChecklistViewModel", "  - Type ID: ${fetchedVehicle?.type?.Id}")
                        android.util.Log.d("ChecklistViewModel", "  - Type Name: ${fetchedVehicle?.type?.Name}")
                        android.util.Log.d("ChecklistViewModel", "  - Status: ${fetchedVehicle?.status}")
                        android.util.Log.d("ChecklistViewModel", "  - Business ID: ${fetchedVehicle?.businessId}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChecklistViewModel", "❌ Error fetching vehicle: ${e.message}", e)
                    null
                }

                // ✅ VALIDATE USER CERTIFICATIONS FOR THIS VEHICLE TYPE
                val currentUser = userRepository.getCurrentUser()
                if (currentUser != null && vehicle != null) {
                    android.util.Log.d("ChecklistViewModel", "🔐 Validating user certification for vehicle type: ${vehicle?.type?.Name}")
                    val validationResult = validateUserCertificationUseCase(currentUser.id, vehicleId.toString())
                    
                    if (!validationResult.isValid) {
                        android.util.Log.e("ChecklistViewModel", "❌ Certification validation failed: ${validationResult.message}")
                        
                        // Update state with certification error
                        withContext(Dispatchers.Main) {
                            _state.update {
                                ChecklistState(
                                    vehicle = vehicle,
                                    vehicleId = vehicleId.toString(),
                                    error = "Certification Required: ${validationResult.message}",
                                    isLoading = false,
                                    noCompatibleChecklists = true, // Use this flag to show special error UI
                                    totalChecklistsFound = 0,
                                    compatibleChecklistsFound = 0,
                                    specificChecklistsFound = 0,
                                    defaultChecklistsFound = 0
                                )
                            }
                        }
                        return@launch // Exit early - don't load checklist
                    } else {
                        android.util.Log.d("ChecklistViewModel", "✅ Certification validation passed: ${validationResult.message}")
                        android.util.Log.d("ChecklistViewModel", "✅ Valid certifications: ${validationResult.validCertifications.map { "${it.name} (expires: ${it.expiryDate})" }}")
                    }
                } else {
                    android.util.Log.w("ChecklistViewModel", "⚠️ Skipping certification validation - currentUser: $currentUser, vehicle: $vehicle")
                }

                // 1. Obtener datos del checklist
                android.util.Log.d("ChecklistViewModel", "📋 Llamando a getChecklistUseCase con vehicleId=$vehicleId")
                val allChecklists = getChecklistUseCase(vehicleId.toString())
                android.util.Log.d("ChecklistViewModel", "📋 Total checklists retrieved from repository: ${allChecklists.size}")
                if (allChecklists.isEmpty()) {
                    android.util.Log.w("ChecklistViewModel", "⚠️ NO CHECKLISTS FOUND AT ALL - Repository returned empty list")
                    android.util.Log.w("ChecklistViewModel", "   This could indicate:")
                    android.util.Log.w("ChecklistViewModel", "   - No checklists configured in the system")
                    android.util.Log.w("ChecklistViewModel", "   - Business context filtering is too restrictive")
                    android.util.Log.w("ChecklistViewModel", "   - Repository query is failing")
                } else {
                    android.util.Log.d("ChecklistViewModel", "📋 Checklists found in repository:")
                    allChecklists.forEachIndexed { index, checklist ->
                        android.util.Log.d("ChecklistViewModel", "  [$index] id=${checklist.id}, title='${checklist.title}'")
                        android.util.Log.d("ChecklistViewModel", "       isDefault=${checklist.isDefault}, allVehicleTypesEnabled=${checklist.allVehicleTypesEnabled}")
                        android.util.Log.d("ChecklistViewModel", "       supportedVehicleTypeIds=${checklist.supportedVehicleTypeIds}")
                        android.util.Log.d("ChecklistViewModel", "       businessId=${checklist.businessId}")
                        android.util.Log.d("ChecklistViewModel", "       items.size=${checklist.items.size}")
                    }
                }
                
                // 2. Filter checklists by BUSINESS CONTEXT first, then by vehicle type
                val vehicleTypeId = vehicle?.type?.Id
                val vehicleTypeName = vehicle?.type?.Name
                android.util.Log.d("ChecklistViewModel", "Vehicle type ID: $vehicleTypeId, Name: $vehicleTypeName")
                android.util.Log.d("ChecklistViewModel", "Current business ID: $businessId")
                
                // STEP 1: Filter by business context (only current business + defaults)
                val businessFilteredChecklists = allChecklists.filter { checklist ->
                    val isBusinessCompatible = (checklist.businessId == businessId) || 
                                             (checklist.businessId == null && checklist.isDefault)
                    android.util.Log.d("ChecklistViewModel", "🏢 [BUSINESS] Checklist ${checklist.id} (${checklist.title}) - " +
                        "checklistBusinessId: ${checklist.businessId}, " +
                        "currentBusinessId: $businessId, " +
                        "isDefault: ${checklist.isDefault}, " +
                        "isBusinessCompatible: $isBusinessCompatible")
                    isBusinessCompatible
                }
                
                android.util.Log.d("ChecklistViewModel", "📊 Business filtered checklists: ${businessFilteredChecklists.size}/${allChecklists.size}")
                businessFilteredChecklists.forEach { checklist ->
                    android.util.Log.d("ChecklistViewModel", "  🏢 ${checklist.title} (businessId: ${checklist.businessId}, isDefault: ${checklist.isDefault})")
                }
                
                // STEP 2: Within business-compatible checklists, prioritize by vehicle type compatibility
                // First, try business-specific checklists that are vehicle type compatible
                val businessSpecificChecklists = businessFilteredChecklists.filter { checklist ->
                    checklist.businessId == businessId
                }
                
                val businessSpecificCompatible = businessSpecificChecklists.filter { checklist ->
                    val isVehicleTypeCompatible = checklist.allVehicleTypesEnabled || 
                                                (vehicleTypeId != null && checklist.supportedVehicleTypeIds.contains(vehicleTypeId))
                    android.util.Log.d("ChecklistViewModel", "🚗 [BUS-SPECIFIC] Checklist ${checklist.id} (${checklist.title}) - " +
                        "allVehicleTypesEnabled: ${checklist.allVehicleTypesEnabled}, " +
                        "supportedTypes: ${checklist.supportedVehicleTypeIds}, " +
                        "vehicleTypeId: $vehicleTypeId, " +
                        "isVehicleTypeCompatible: $isVehicleTypeCompatible")
                    isVehicleTypeCompatible
                }
                
                // Then, look for default checklists that are vehicle type compatible
                val defaultChecklists = businessFilteredChecklists.filter { checklist ->
                    checklist.businessId == null && checklist.isDefault
                }
                
                val defaultCompatible = defaultChecklists.filter { checklist ->
                    val isVehicleTypeCompatible = checklist.allVehicleTypesEnabled || 
                                                (vehicleTypeId != null && checklist.supportedVehicleTypeIds.contains(vehicleTypeId))
                    android.util.Log.d("ChecklistViewModel", "🔄 [DEFAULT] Checklist ${checklist.id} (${checklist.title}) - " +
                        "allVehicleTypesEnabled: ${checklist.allVehicleTypesEnabled}, " +
                        "supportedTypes: ${checklist.supportedVehicleTypeIds}, " +
                        "vehicleTypeId: $vehicleTypeId, " +
                        "isVehicleTypeCompatible: $isVehicleTypeCompatible")
                    isVehicleTypeCompatible
                }
                
                // STEP 3: Prioritize business-specific over defaults
                val compatibleChecklists = if (businessSpecificCompatible.isNotEmpty()) {
                    android.util.Log.d("ChecklistViewModel", "✅ Using ${businessSpecificCompatible.size} business-specific checklists for vehicle type $vehicleTypeName")
                    businessSpecificCompatible.forEach { checklist ->
                        android.util.Log.d("ChecklistViewModel", "  ✅ Business-specific: ${checklist.title} (ID: ${checklist.id})")
                    }
                    businessSpecificCompatible
                } else if (defaultCompatible.isNotEmpty()) {
                    android.util.Log.d("ChecklistViewModel", "⚠️ No business-specific checklists found, using ${defaultCompatible.size} default checklists for vehicle type $vehicleTypeName")
                    defaultCompatible.forEach { checklist ->
                        android.util.Log.d("ChecklistViewModel", "  ⚠️ Default: ${checklist.title} (ID: ${checklist.id}, isDefault: ${checklist.isDefault})")
                    }
                    defaultCompatible
                } else {
                    android.util.Log.e("ChecklistViewModel", "❌ No compatible checklists found for vehicle type $vehicleTypeName in business context")
                    emptyList()
                }
                
                android.util.Log.d("ChecklistViewModel", "📊 [RESULTADO FINAL] Compatible checklists: ${compatibleChecklists.size}")
                compatibleChecklists.forEachIndexed { index, checklist ->
                    android.util.Log.d("ChecklistViewModel", "  ✅ [$index] ${checklist.title} (ID: ${checklist.id}, isDefault: ${checklist.isDefault}, allVehicleTypesEnabled: ${checklist.allVehicleTypesEnabled})")
                }
                
                // Check if no compatible checklists were found at all
                val noCompatibleChecklists = allChecklists.isNotEmpty() && compatibleChecklists.isEmpty()
                if (noCompatibleChecklists) {
                    android.util.Log.e("ChecklistViewModel", "❌ NO HAY CHECKLISTS COMPATIBLES para vehicleTypeId=$vehicleTypeId ($vehicleTypeName)")
                    android.util.Log.e("ChecklistViewModel", "   - Total checklists: ${allChecklists.size}")
                    android.util.Log.e("ChecklistViewModel", "   - Business filtered: ${businessFilteredChecklists.size}")
                    android.util.Log.e("ChecklistViewModel", "   - Business-specific compatible: ${businessSpecificCompatible.size}")
                    android.util.Log.e("ChecklistViewModel", "   - Default compatible: ${defaultCompatible.size}")
                } else {
                    android.util.Log.d("ChecklistViewModel", "✅ CHECKLISTS COMPATIBLES ENCONTRADOS: ${compatibleChecklists.size}")
                }
                
                // 3. Filter questions within checklists by vehicle type
                val filteredChecklists = compatibleChecklists.map { checklist ->
                    val filteredItems = checklist.items.filter { item ->
                        val itemCompatible = when {
                            // If item has allVehicleTypesEnabled = true, it's compatible with all vehicles
                            item.allVehicleTypesEnabled -> {
                                android.util.Log.d("ChecklistViewModel", "Item ${item.id} (${item.question}) - allVehicleTypesEnabled=true, compatible with all vehicles")
                                true
                            }
                            // If item has no vehicle type restrictions, it's compatible with all
                            item.supportedVehicleTypeIds.isEmpty() -> {
                                android.util.Log.d("ChecklistViewModel", "Item ${item.id} (${item.question}) - no vehicle type restrictions, compatible with all")
                                true
                            }
                            // If vehicle type is null, show all items
                            vehicleTypeId == null -> {
                                android.util.Log.d("ChecklistViewModel", "Item ${item.id} (${item.question}) - vehicleTypeId is null, showing all items")
                                true
                            }
                            // Check if this vehicle type is in the supported list
                            else -> {
                                val isSupported = item.supportedVehicleTypeIds.contains(vehicleTypeId)
                                android.util.Log.d("ChecklistViewModel", "Item ${item.id} (${item.question}) - checking supportedVehicleTypeIds: ${item.supportedVehicleTypeIds}, vehicleTypeId: $vehicleTypeId, isSupported: $isSupported")
                                isSupported
                            }
                        }
                        android.util.Log.d("ChecklistViewModel", "Item ${item.id} (${item.question}) - " +
                            "allVehicleTypesEnabled: ${item.allVehicleTypesEnabled}, " +
                            "supportedVehicleTypeIds: ${item.supportedVehicleTypeIds}, " +
                            "vehicleTypeId: $vehicleTypeId, " +
                            "isCompatible: $itemCompatible")
                        itemCompatible
                    }
                    android.util.Log.d("ChecklistViewModel", "Checklist '${checklist.title}': ${checklist.items.size} items -> ${filteredItems.size} compatible items")
                    checklist.copy(items = filteredItems)
                }
                
                val firstChecklist = filteredChecklists.firstOrNull()
                if (firstChecklist == null) {
                    if (noCompatibleChecklists) {
                        android.util.Log.w("ChecklistViewModel", "No hay checklists compatibles para vehicleId=$vehicleId, vehicleTypeId=$vehicleTypeId ($vehicleTypeName)")
                    } else {
                        android.util.Log.d("ChecklistViewModel", "No se encontró ningún checklist para vehicleId=$vehicleId")
                    }
                }
                val allItems = filteredChecklists.flatMap { it.items }
                android.util.Log.d("ChecklistViewModel", "Total de items después del filtrado: ${allItems.size}")
                android.util.Log.d("ChecklistViewModel", "All item categories: ${allItems.map { it.category }}")
                
                allItems.forEachIndexed { index, item ->
                    android.util.Log.d("ChecklistViewModel", "Item $index: id=${item.id}, question=${item.question.take(50)}..., isCritical=${item.isCritical}")
                }
                val selectedItems = if (firstChecklist != null) {
                    android.util.Log.d("ChecklistViewModel", "Seleccionando preguntas para rotación con parámetros:")
                    android.util.Log.d("ChecklistViewModel", "  - criticalQuestionMinimum: ${firstChecklist.criticalQuestionMinimum}")
                    android.util.Log.d("ChecklistViewModel", "  - maxQuestionsPerCheck: ${firstChecklist.maxQuestionsPerCheck}")
                    android.util.Log.d("ChecklistViewModel", "  - standardQuestionMaximum: ${firstChecklist.standardQuestionMaximum}")
                    selectQuestionsForRotation(
                        allItems,
                        firstChecklist.criticalQuestionMinimum,
                        firstChecklist.maxQuestionsPerCheck,
                        firstChecklist.standardQuestionMaximum
                    )
                } else {
                    android.util.Log.w("ChecklistViewModel", "No hay firstChecklist, retornando lista vacía")
                    emptyList()
                }
                android.util.Log.d("ChecklistViewModel", "Items seleccionados para rotación: ${selectedItems.size}")
                selectedItems.forEachIndexed { index, item ->
                    android.util.Log.d("ChecklistViewModel", "Selected item $index: ${item.question.take(50)}..., isCritical=${item.isCritical}")
                }

                // 2. Crear o recuperar el check
                android.util.Log.d("ChecklistViewModel", "Llamando a getLastPreShiftCheck para vehicleId=$vehicleId, businessId=$businessId")
                val lastCheck = try {
                    checklistRepository.getLastPreShiftCheck(vehicleId.toString(), businessId ?: "").also {
                        android.util.Log.d("ChecklistViewModel", "lastCheck: $it")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChecklistViewModel", "Error fetching last pre-shift check: ${e.message}", e)
                    null
                }
                
                // Get current time in ISO format
                val currentDateTime = java.time.Instant.now().toString()
                android.util.Log.d("ChecklistViewModel", "Setting initial startDateTime: $currentDateTime")

                val prevChecklistAnswerId = _state.value?.checklistAnswerId
                val checkId = if (lastCheck?.status == CheckStatus.IN_PROGRESS.toString()) {
                    lastCheck.id
                } else prevChecklistAnswerId

                withContext(Dispatchers.Main) {
                    android.util.Log.d("ChecklistViewModel", "🎯 Setting final ChecklistState")
                    android.util.Log.d("ChecklistViewModel", "  - Vehicle: ${vehicle?.codename} (${vehicle?.type?.Name})")
                    android.util.Log.d("ChecklistViewModel", "  - Selected items: ${selectedItems.size}")
                    android.util.Log.d("ChecklistViewModel", "  - ChecklistAnswerId: $checkId")
                    android.util.Log.d("ChecklistViewModel", "  - FirstChecklist: ${firstChecklist?.title} (${firstChecklist?.id})")
                    android.util.Log.d("ChecklistViewModel", "  - NoCompatibleChecklists: $noCompatibleChecklists")
                    
                    _state.value = ChecklistState(
                        vehicle = vehicle,
                        vehicleId = vehicleId.toString(),
                        vehicleStatus = VehicleStatus.AVAILABLE,
                        checkStatus = CheckStatus.NOT_STARTED.toString(),
                        checkItems = selectedItems,
                        checklistAnswerId = checkId,
                        checklistId = selectedItems.firstOrNull()?.checklistId,
                        checklistVersion = firstChecklist?.version ?: "1.0", // ✅ NEW: Set checklist version from the first checklist
                        startDateTime = currentDateTime,
                        noCompatibleChecklists = noCompatibleChecklists,
                        totalChecklistsFound = allChecklists.size,
                        compatibleChecklistsFound = compatibleChecklists.size,
                        specificChecklistsFound = businessSpecificCompatible.size,
                        defaultChecklistsFound = defaultCompatible.size
                    )
                    
                    android.util.Log.d("ChecklistViewModel", "=== 🎯 FINAL UI STATE ===")
                    android.util.Log.d("ChecklistViewModel", "📊 Statistics:")
                    android.util.Log.d("ChecklistViewModel", "  - Total checklists found: ${allChecklists.size}")
                    android.util.Log.d("ChecklistViewModel", "  - Business filtered checklists: ${businessFilteredChecklists.size}")
                    android.util.Log.d("ChecklistViewModel", "  - Compatible checklists: ${compatibleChecklists.size}")
                    android.util.Log.d("ChecklistViewModel", "  - Business-specific checklists: ${businessSpecificCompatible.size}")
                    android.util.Log.d("ChecklistViewModel", "  - Default checklists: ${defaultCompatible.size}")
                    android.util.Log.d("ChecklistViewModel", "  - Selected items for rotation: ${selectedItems.size}")
                    android.util.Log.d("ChecklistViewModel", "🚨 UI Flags:")
                    android.util.Log.d("ChecklistViewModel", "  - noCompatibleChecklists: $noCompatibleChecklists")
                    android.util.Log.d("ChecklistViewModel", "  - Will show 'No checklist items': ${selectedItems.isEmpty()}")
                    android.util.Log.d("ChecklistViewModel", "  - Will show 'Checklist without questions': ${selectedItems.isEmpty() && compatibleChecklists.isNotEmpty()}")
                    android.util.Log.d("ChecklistViewModel", "  - Will show 'No checklists configured': ${allChecklists.isEmpty()}")
                    android.util.Log.d("ChecklistViewModel", "================================")
                    
                    maybeStartTimer()

                    // --- Limpieza opcional de imágenes locales que ya no correspondan a preguntas actuales ---
                    val currentIds = selectedItems.map { it.id }.toSet()
                    _itemImages.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploading.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploadErrors.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploadedMultimedia.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploadingImages.update { oldSet ->
                        oldSet.filter { uri ->
                            _itemImages.value.any { (itemId, uris) ->
                                itemId in currentIds && uris.any { it == uri.toString() }
                            }
                        }.toSet()
                    }

                    // Poblar mapping de answeredItemIds para todos los items respondidos
                    val currentChecklistAnswerId = state.value?.checklistAnswerId
                    if (currentChecklistAnswerId == null) {
                        android.util.Log.d("appflow", "[Mapping][SKIP] checklistAnswerId is null, not updating mapping")
                        return@withContext
                    }
                    val answeredItems = answeredChecklistItemRepository.getAll().filter { it.checklistAnswerId == currentChecklistAnswerId }
                    val mapping = state.value?.checkItems?.mapNotNull { item ->
                        val answered = answeredItems.find { ai -> ai.checklistItemId == item.id }
                        answered?.let { item.id to it.id }
                    }?.toMap() ?: emptyMap()
                    _answeredItemIds.value = mapping
                    logAnsweredItemIdsState()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.util.Log.e("ChecklistViewModel", "Error al cargar checklist: ${e.message}", e)
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
        criticalQuestionMinimum: Int,
        maxQuestionsPerCheck: Int,
        standardQuestionMaximum: Int
    ): List<ChecklistItem> {
        val selectedQuestions = mutableListOf<ChecklistItem>()
        // Get critical questions
        val criticalQuestions = allItems
            .filter { it.isCritical }
            .shuffled()
            .take(criticalQuestionMinimum)
        selectedQuestions.addAll(criticalQuestions)
        // Fill remaining slots with standard questions
        val remainingSlots = maxQuestionsPerCheck - selectedQuestions.size
        if (remainingSlots > 0) {
            val standardQuestions = allItems
                .filter { 
                    !it.isCritical && 
                    !selectedQuestions.contains(it) 
                }
                .shuffled()
                .take(minOf(remainingSlots, standardQuestionMaximum))
            selectedQuestions.addAll(standardQuestions)
        }
        return selectedQuestions
    }

    fun updateItemResponse(id: String, isYes: Boolean) {
        if (state.value?.isReadOnly == true) {
            android.util.Log.d("ChecklistFields", "Cannot update response - Checklist is readonly")
            return
        }

        android.util.Log.d("ChecklistFields", "Updating response for item[$id] - isYes: $isYes")
        viewModelScope.launch {
            try {
                val currentItems = state.value?.checkItems?.toMutableList() ?: mutableListOf()
                val itemIndex = currentItems.indexOfFirst { it.id == id }

                if (itemIndex != -1) {
                    val item = currentItems[itemIndex]
                    val newAnswer = if (isYes) Answer.PASS else Answer.FAIL
                    
                    // Create a new item with the updated answer
                    val updatedItem = item.copy(userAnswer = newAnswer)
                    currentItems[itemIndex] = updatedItem
                    
                    android.util.Log.d("ChecklistFields", "Item[$id] response updated to: $newAnswer")

                    // Get current checklist answer ID or create a new one
                    var checklistAnswerId = state.value?.checklistAnswerId
                    if (checklistAnswerId == null) {
                        val validation = validateChecklistUseCase(currentItems)
                        val checklistAnswer = createOrUpdateChecklistAnswer(validation.status)
                        val savedAnswer = checklistAnswerRepository.save(checklistAnswer)
                        checklistAnswerId = savedAnswer.id
                    }

                    // Create and save the answered checklist item
                    val existingAnsweredItem = try {
                        answeredChecklistItemRepository.getAll().find {
                            it.checklistAnswerId == (checklistAnswerId ?: "") && it.checklistItemId == item.id
                        }
                    } catch (e: Exception) { null }

                    // Get business context using BusinessContextManager
                    val businessId = businessContextManager.getCurrentBusinessId()
                    android.util.Log.d("ChecklistViewModel", "Creating AnsweredChecklistItem with businessId: $businessId")
                    
                    val answeredItem = AnsweredChecklistItem(
                        id = existingAnsweredItem?.id ?: java.util.UUID.randomUUID().toString(),
                        checklistId = item.checklistId,
                        checklistVersion = state.value?.checklistVersion ?: "1.0", // ✅ FIXED: Use checklistVersion from state
                        checklistAnswerId = checklistAnswerId ?: "",
                        checklistItemId = item.id,
                        checklistItemVersion = item.version, // ✅ NEW: Include question version
                        question = item.question,
                        answer = newAnswer.name,
                        userId = userRepository.getCurrentUser()?.id ?: "",
                        createdAt = java.time.Instant.now().toString(),
                        isNew = existingAnsweredItem == null,
                        isDirty = true,
                        userComment = item.userComment,
                        businessId = businessId
                    )

                    // Save the answered item
                    answeredChecklistItemRepository.save(answeredItem)

                    // Update the state atomically with all changes
                    val validation = validateChecklistUseCase(currentItems)
                    _state.update { currentState ->
                        android.util.Log.d("ChecklistFields", "State updated with new response for item[$id] - " +
                            "isComplete: ${validation.isComplete}, " +
                            "isBlocked: ${validation.isBlocked}")
                        currentState?.copy(
                            checkItems = currentItems,
                            isCompleted = validation.isComplete,
                            vehicleBlocked = validation.isBlocked,
                            hasUnsavedChanges = true,
                            lastSyncedItemId = null,
                            checklistAnswerId = checklistAnswerId
                        )
                    }

                    // Update the mapping
                    _answeredItemIds.update { it + (item.id to answeredItem.id) }
                    logAnsweredItemIdsState()

                    // Repopulate the mapping for all items (filtered by checklistAnswerId)
                    val currentChecklistAnswerId = state.value?.checklistAnswerId
                    if (currentChecklistAnswerId == null) {
                        android.util.Log.d("appflow", "[Mapping][SKIP] checklistAnswerId is null, not updating mapping")
                        return@launch
                    }
                    val answeredItems = answeredChecklistItemRepository.getAll().filter { it.checklistAnswerId == currentChecklistAnswerId }
                    val mapping = state.value?.checkItems?.mapNotNull { item ->
                        val answered = answeredItems.find { ai -> ai.checklistItemId == item.id }
                        answered?.let { item.id to it.id }
                    }?.toMap() ?: emptyMap()
                    _answeredItemIds.value = mapping
                    logAnsweredItemIdsState()

                    // 1. When an AnsweredChecklistItem is created or updated in updateItemResponse:
                    android.util.Log.d("appflow", "[AnsweredChecklistItem] Saved: id=${answeredItem.id}, itemId=${item.id}, checklistAnswerId=${answeredItem.checklistAnswerId}, userId=${answeredItem.userId}")
                } else {
                    android.util.Log.w("ChecklistFields", "Item[$id] not found when trying to update response")
                }
            } catch (e: Exception) {
                android.util.Log.e("ChecklistFields", "Error updating item response: ${e.message}", e)
                _state.update { it?.copy(
                    error = "Failed to update answer: ${e.message}",
                    showErrorModal = true
                ) }
            }
        }
    }

    private suspend fun syncItemChange(itemId: String, items: List<ChecklistItem>) {
        android.util.Log.d(TAG, "syncItemChange: Starting sync for item $itemId")
        try {
            // Get current location
            val locationState = locationManager.locationState.value
            android.util.Log.d("ChecklistViewModel", "syncItemChange: Location state - $locationState")
            
            // Verify we have location before proceeding
            if (locationState.location == null) {
                android.util.Log.w("ChecklistViewModel", "syncItemChange: Location is null, requesting update")
                locationManager.requestSingleUpdate()
            }

            val locationCoordinates = locationState.location
            android.util.Log.d("ChecklistViewModel", "syncItemChange: Using coordinates - $locationCoordinates")

            // Add validation for current items
            val validation = validateChecklistUseCase(items)
            // Create or update ChecklistAnswer
            val checklistAnswer = createOrUpdateChecklistAnswer(validation.status)
            android.util.Log.d("ChecklistViewModel", "syncItemChange: Created/Updated checklist answer with ID: ${checklistAnswer.id}")
            android.util.Log.d("ChecklistViewModel", "syncItemChange: checklistAnswer.checklistId: ${checklistAnswer.checklistId}")

            // Save ChecklistAnswer
            val savedAnswer = checklistAnswerRepository.save(checklistAnswer)
            android.util.Log.d("ChecklistViewModel", "syncItemChange: Saved checklist answer with ID: ${savedAnswer.id}")

            // Update state with new checklistAnswerId and ensure checklistId is preserved
            _state.update { currentState ->
                currentState?.copy(
                    checklistAnswerId = savedAnswer.id,
                    checklistId = savedAnswer.checklistId // Mantener el ID de la plantilla
                )
            }

            // Save individual answer
            val item = items.find { it.id == itemId }
            if (item?.userAnswer != null) {
                android.util.Log.d(TAG, "syncItemChange: Creating AnsweredChecklistItem for item $itemId with answer ${item.userAnswer} and userComment=${item.userComment}")
                val existingAnsweredItem = try {
                    answeredChecklistItemRepository.getAll().find {
                        it.checklistAnswerId == savedAnswer.id && it.checklistItemId == item.id
                    }
                } catch (e: Exception) { null }
                val answeredItem = AnsweredChecklistItem(
                    id = existingAnsweredItem?.id ?: java.util.UUID.randomUUID().toString(),
                    checklistId = item.checklistId, // ID de la plantilla
                    checklistVersion = state.value?.checklistVersion ?: "1.0", // ✅ FIXED: Use checklistVersion from state
                    checklistAnswerId = savedAnswer.id, // ID de la instancia de respuesta
                    checklistItemId = item.id, // Set checklistItemId to ChecklistItem's id
                    checklistItemVersion = item.version, // ✅ NEW: Include question version
                    question = item.question,
                    answer = item.userAnswer.name,
                    userId = userRepository.getCurrentUser()?.id ?: "",
                    createdAt = java.time.Instant.now().toString(),
                    isNew = existingAnsweredItem == null,
                    isDirty = true,
                    userComment = item.userComment
                )
                android.util.Log.d(TAG, "syncItemChange: Saving AnsweredChecklistItem - id: ${answeredItem.id}, checklistAnswerId: ${answeredItem.checklistAnswerId}, userComment: ${answeredItem.userComment}")
                answeredChecklistItemRepository.save(answeredItem)
                android.util.Log.d("ChecklistViewModel", "syncItemChange: AnsweredChecklistItem saved successfully")
            } else {
                android.util.Log.w("ChecklistViewModel", "syncItemChange: Item $itemId not found or has no answer")
            }

            // Update sync state
            _state.update { currentState ->
                currentState?.copy(
                    hasUnsavedChanges = false,
                    syncErrors = currentState.syncErrors - itemId,
                    lastSyncedItemId = itemId
                )
            }
            android.util.Log.d("ChecklistViewModel", "syncItemChange: Sync completed successfully for item $itemId")
            
        } catch (e: Exception) {
            android.util.Log.e("ChecklistViewModel", "syncItemChange: Error syncing item $itemId - ${e.message}", e)
            throw e
        }
    }

    private suspend fun syncAllPendingChanges() {
        android.util.Log.d("ChecklistViewModel", "syncAllPendingChanges: Starting")
        val currentItems = state.value?.checkItems ?: return
        val itemsWithErrors = state.value?.syncErrors?.keys ?: emptySet()
        
        android.util.Log.d("ChecklistViewModel", "syncAllPendingChanges: Found ${itemsWithErrors.size} items with errors")
        
        for (itemId in itemsWithErrors) {
            try {
                android.util.Log.d("ChecklistViewModel", "syncAllPendingChanges: Attempting to sync item $itemId")
                syncItemChange(itemId, currentItems)
                android.util.Log.d("ChecklistViewModel", "syncAllPendingChanges: Successfully synced item $itemId")
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "syncAllPendingChanges: Error syncing item $itemId - ${e.message}")
                throw Exception("Failed to sync item $itemId: ${e.message}")
            }
        }
        android.util.Log.d("ChecklistViewModel", "syncAllPendingChanges: Completed successfully")
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

    /**
     * Handle initial hour meter confirmation and start vehicle session
     */
    fun onInitialHourMeterConfirmed(hourMeter: String) {
        viewModelScope.launch {
            try {
                _state.update { 
                    it?.copy(
                        showInitialHourMeterDialog = false,
                        isLoading = true
                    ) 
                }
                
                val vehicleId = state.value?.vehicleId ?: throw Exception("Vehicle ID not found")
                val checklistAnswerId = state.value?.pendingChecklistAnswerId ?: throw Exception("Checklist Answer ID not found")
                
                android.util.Log.d("ChecklistViewModel", "Starting vehicle session with initial hour meter: $hourMeter")
                val result = startVehicleSessionUseCase(vehicleId, checklistAnswerId, hourMeter)
                
                result.onSuccess {
                    android.util.Log.d("ChecklistViewModel", "Vehicle session started successfully with hour meter.")
                    val currentUser = userRepository.getCurrentUser()
                    val role = currentUser?.role?.name ?: "operator"
                    android.util.Log.d("ChecklistViewModel", "Emitting navigation event to dashboard for role: $role")
                    _navigationEvent.value = NavigationEvent.AfterSubmit(role = role)
                }.onFailure { e ->
                    android.util.Log.e("ChecklistViewModel", "Failed to start vehicle session: ${e.message}", e)
                    _state.update { 
                        it?.copy(
                            isLoading = false,
                            message = "Failed to start vehicle session: ${e.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error handling hour meter confirmation: ${e.message}", e)
                _state.update { 
                    it?.copy(
                        isLoading = false,
                        showInitialHourMeterDialog = false,
                        message = "Error: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Dismiss initial hour meter dialog
     */
    fun onInitialHourMeterDismissed() {
        _state.update { 
            it?.copy(
                showInitialHourMeterDialog = false,
                pendingChecklistAnswerId = null
            ) 
        }
    }

    fun resetNavigation() {
        _navigationEvent.value = null
    }

    private suspend fun createOrUpdateChecklistAnswer(status: CheckStatus): ChecklistAnswer {
        android.util.Log.d("ChecklistViewModel", "Creating/Updating checklist answer")
        val now = java.time.Instant.now().toString()
        val userId = userRepository.getCurrentUser()?.id ?: ""
        val locationCoordinates = locationManager.locationState.value.location
        val currentVehicleId = state.value?.vehicleId ?: ""
        
        // Get business context using BusinessContextManager
        val businessId = businessContextManager.getCurrentBusinessId()
        android.util.Log.d("ChecklistViewModel", "Using businessId: $businessId for ChecklistAnswer")

        // Obtener el ID del checklist actual (plantilla)
        val currentChecklistId = state.value?.checklistId 
            ?: state.value?.checkItems?.firstOrNull()?.checklistId
            ?: throw Exception("No checklist ID found")
        android.util.Log.d("ChecklistViewModel", "Current checklist ID (plantilla): $currentChecklistId")

        // Calcular duración en segundos
        val durationSeconds = ((state.value?.elapsedTime ?: 0L) / 1000).toInt().coerceAtLeast(0)

        // Intentar obtener el ChecklistAnswer existente si hay un answerId
        val existingAnswerId = state.value?.checklistAnswerId
        android.util.Log.d("ChecklistViewModel", "Existing checklist answer ID: $existingAnswerId")

        if (existingAnswerId != null) {
            try {
                android.util.Log.d("ChecklistViewModel", "Attempting to get existing checklist answer with ID: $existingAnswerId")
                val existingAnswer = checklistAnswerRepository.getById(existingAnswerId)
                if (existingAnswer != null) {
                    android.util.Log.d("ChecklistViewModel", "Found existing checklist answer, updating it")
                    return existingAnswer.copy(
                        endDateTime = now,
                        status = status.toApiInt(),
                        checklistId = currentChecklistId,
                        checklistVersion = state.value?.checklistVersion ?: "1.0", // ✅ FIXED: Use checklistVersion from state
                        locationCoordinates = locationCoordinates,
                        lastCheckDateTime = now,
                        vehicleId = currentVehicleId,
                        duration = durationSeconds,
                        businessId = businessId
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error getting existing checklist answer: ${e.message}")
                // Si hay error al obtener el existente, continuamos creando uno nuevo
            }
        }

        // Si no existe o hubo error, creamos uno nuevo
        android.util.Log.d("ChecklistViewModel", "Creating new checklist answer")
        return ChecklistAnswer(
            id = java.util.UUID.randomUUID().toString(),
            checklistId = currentChecklistId,
            checklistVersion = state.value?.checklistVersion ?: "1.0", // ✅ NEW: Include checklist version
            goUserId = userId,
            startDateTime = state.value?.startDateTime ?: now,
            endDateTime = now,
            status = status.toApiInt(),
            locationCoordinates = locationCoordinates,
            isDirty = true,
            isNew = true,
            isMarkedForDeletion = false,
            lastCheckDateTime = now,
            vehicleId = currentVehicleId,
            duration = durationSeconds,
            businessId = businessId
        )
    }

    fun submitCheck() {
        if (state.value?.isReadOnly == true) {
            android.util.Log.w("ChecklistViewModel", "Attempt to submit readonly checklist - operation cancelled")
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it?.copy(isSubmitting = true) }
                
                val currentItems = state.value?.checkItems ?: mutableListOf()
                
                // Validate checklist
                val validation = validateChecklistUseCase(items = currentItems)
                
                if (!validation.isComplete) {
                    _state.update { 
                        it?.copy(
                            showErrorModal = true,
                            errorModalMessage = "Please complete all required questions before finishing",
                            isSubmitting = false
                        )
                    }
                    return@launch
                }

                // Check for unsaved changes or sync errors
                if (state.value?.needsSync == true) {
                    try {
                        syncAllPendingChanges()
                    } catch (e: Exception) {
                        android.util.Log.e("ChecklistViewModel", "Failed to sync pending changes: ${e.message}", e)
                        _state.update {
                            it?.copy(
                                isSubmitting = false,
                                showErrorModal = true,
                                errorModalMessage = "Error al sincronizar cambios pendientes: ${e.message}"
                            )
                        }
                        return@launch
                    }
                }

                // Get current location
                val locationState = locationManager.locationState.value
                
                // Verify we have location before proceeding
                if (locationState.location == null) {
                    locationManager.requestSingleUpdate()
                    _state.update { 
                        it?.copy(
                            showErrorModal = true,
                            errorModalMessage = "Location is required to submit the checklist. Please verify that location is enabled.",
                            isSubmitting = false
                        )
                    }
                    return@launch
                }

                val locationCoordinates = locationState.location

                // Create or update ChecklistAnswer with correct status
                val checklistAnswer = createOrUpdateChecklistAnswer(validation.status)
                android.util.Log.d("ChecklistViewModel", "Saving checklist answer with ID: ${checklistAnswer.id}")
                
                val savedChecklistAnswer = checklistAnswerRepository.save(checklistAnswer)
                android.util.Log.d("ChecklistViewModel", "Successfully saved checklist answer with ID: ${savedChecklistAnswer.id}")

                // Actualizar el estado con el nuevo checklistAnswerId y mantener el checklistId
                _state.update { currentState ->
                    currentState?.copy(
                        checklistAnswerId = savedChecklistAnswer.id,
                        checklistId = savedChecklistAnswer.checklistId
                    )
                }

                // Submit final checklist
                val updatedCheck = submitChecklistUseCase(
                    vehicleId = vehicleId.toString(),
                    items = currentItems,
                    checkId = savedChecklistAnswer.id,
                    status = validation.status.name,
                    location = locationState.location,
                    locationCoordinates = locationCoordinates
                )

                // --- NUEVO: Crear SafetyAlert por cada pregunta no crítica respondida como FAIL ---
                val failedNonCriticalItems = currentItems.filter { !it.isCritical && it.userAnswer == Answer.FAIL }
                val currentUser = userRepository.getCurrentUser()
                val now = java.time.Instant.now().toString()
                for (item in failedNonCriticalItems) {
                    try {
                        val answeredItemId = answeredItemIds.value[item.id]
                        if (answeredItemId == null) {
                            android.util.Log.e("ChecklistViewModel", "No answeredItemId found for item ${item.id}")
                            continue
                        }
                        val alert = app.forku.data.api.dto.safetyalert.SafetyAlertDto(
                            id = java.util.UUID.randomUUID().toString(),
                            answeredChecklistItemId = answeredItemId,
                            goUserId = currentUser?.id ?: "",
                            vehicleId = vehicleId.toString(),
                            isDirty = true,
                            isNew = true,
                            isMarkedForDeletion = false,
                            creationDateTime = now
                        )
                        val result = createSafetyAlertUseCase(alert)
                        android.util.Log.d("ChecklistViewModel", "SafetyAlert created for item ${item.id}: ${result?.id}")
                    } catch (e: Exception) {
                        android.util.Log.e("ChecklistViewModel", "Error creating SafetyAlert for item ${item.id}: ${e.message}", e)
                    }
                }

                // Start vehicle session if checklist passed
                if (validation.canStartSession) {
                    try {
                        val result = startVehicleSessionUseCase(vehicleId.toString(), updatedCheck.id)
                        result.onSuccess {
                            android.util.Log.d("ChecklistViewModel", "Vehicle session started successfully via use case.")
                            val currentUser = userRepository.getCurrentUser()
                            val role = currentUser?.role?.name ?: "operator"
                            android.util.Log.d("ChecklistViewModel", "Emitting navigation event to dashboard for role: $role")
                            _navigationEvent.value = NavigationEvent.AfterSubmit(role = role)
                        }.onFailure { e ->
                            android.util.Log.e("ChecklistViewModel", "Error starting vehicle session via use case: ${e.message}", e)
                            _state.update {
                                it?.copy(
                                    showErrorModal = true,
                                    errorModalMessage = "Check completed but could not start session: ${e.message}"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChecklistViewModel", "Error starting vehicle session: ${e.message}", e)
                        _state.update {
                            it?.copy(
                                showErrorModal = true,
                                errorModalMessage = "Check completed but could not start session: ${e.message}"
                            )
                        }
                    }
                } else if (validation.status == CheckStatus.COMPLETED_FAIL) {
                    // Bloquear vehículo ya ocurre en repositorio, solo navega y muestra mensaje
                    _state.update { it?.copy(vehicleBlocked = true, message = "The vehicle has been blocked due to a checklist failure.") }
                    _navigationEvent.value = NavigationEvent.VehicleBlocked
                    // Cambiar estado del vehículo a OUT_OF_SERVICE
                    val vehicleId = state.value?.vehicleId
                    if (vehicleId != null) {
                        val businessId = businessContextManager.getCurrentBusinessId() ?: state.value?.vehicle?.businessId ?: ""
                        vehicleStatusUpdater.updateVehicleStatus(vehicleId, VehicleStatus.OUT_OF_SERVICE, businessId)
                    }
                }

                // Update final state and navigate
                _state.update { 
                    it?.copy(
                        isSubmitting = false,
                        isCompleted = true,
                        checkItems = updatedCheck.items,
                        checkStatus = updatedCheck.status,
                        isSubmitted = true,
                        hasUnsavedChanges = false,
                        syncErrors = emptyMap()
                    )
                }
                stopTimerIfCompleted()

            } catch (e: Exception) {
                android.util.Log.e("ChecklistViewModel", "Error in submitCheck: ${e.message}", e)
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
        android.util.Log.d("ChecklistViewModel", "loadExistingCheck A")
        viewModelScope.launch {
            try {
                val check = checklistRepository.getCheckById(checkId)
                val vehicle = getVehicleUseCase(vehicleId.toString())
                
                check?.let {
                    val prevChecklistAnswerId = _state.value?.checklistAnswerId
                    _state.value = ChecklistState(
                        vehicle = vehicle,
                        vehicleId = vehicleId.toString(),
                        vehicleStatus = VehicleStatus.IN_USE,
                        checkItems = it.items,
                        checklistAnswerId = it.id ?: prevChecklistAnswerId,
                        checklistId = it.items.firstOrNull()?.checklistId,
                        checkStatus = it.status,
                        isCompleted = it.status != CheckStatus.IN_PROGRESS.toString(),
                        isSubmitted = it.status != CheckStatus.IN_PROGRESS.toString(),
                        isReadOnly = it.status != CheckStatus.IN_PROGRESS.toString(),
                        startDateTime = it.startDateTime
                    )
                    maybeStartTimer()

                    // --- Limpieza opcional de imágenes locales que ya no correspondan a preguntas actuales ---
                    val currentIds = it.items.map { item -> item.id }.toSet()
                    _itemImages.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploading.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploadErrors.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploadedMultimedia.update { oldMap ->
                        oldMap.filterKeys { it in currentIds }
                    }
                    _uploadingImages.update { oldSet ->
                        oldSet.filter { uri ->
                            _itemImages.value.any { (itemId, uris) ->
                                itemId in currentIds && uris.any { it == uri.toString() }
                            }
                        }.toSet()
                    }

                    // Poblar mapping de answeredItemIds para todos los items respondidos
                    val currentChecklistAnswerId = state.value?.checklistAnswerId
                    if (currentChecklistAnswerId == null) {
                        android.util.Log.d("appflow", "[Mapping][SKIP] checklistAnswerId is null, not updating mapping")
                        return@launch
                    }
                    val answeredItems = answeredChecklistItemRepository.getAll().filter { it.checklistAnswerId == currentChecklistAnswerId }
                    val mapping = it.items.mapNotNull { item ->
                        val answered = answeredItems.find { ai -> ai.checklistItemId == item.id }
                        answered?.let { item.id to it.id }
                    }.toMap()
                    _answeredItemIds.value = mapping
                    logAnsweredItemIdsState()
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

    /**
     * Saves the current ChecklistAnswer to the backend using the repository.
     * Updates state with loading, error, and success as appropriate.
     */
    fun saveChecklistAnswer() {
        viewModelScope.launch {
            _state.update { it?.copy(isLoading = true, error = null, message = null) }
            try {
                val currentItems = state.value?.checkItems ?: emptyList()
                val validation = validateChecklistUseCase(currentItems)
                val checklistAnswer = createOrUpdateChecklistAnswer(validation.status)
                val savedAnswer = checklistAnswerRepository.save(checklistAnswer)
                _state.update { currentState ->
                    currentState?.copy(
                        checklistAnswerId = savedAnswer.id,
                        checklistId = savedAnswer.checklistId,
                        isLoading = false,
                        message = "Checklist answer saved successfully"
                    )
                }

                // --- NUEVO: Crear SafetyAlert por cada pregunta no crítica respondida como FAIL ---
                val failedNonCriticalItems = currentItems.filter { !it.isCritical && it.userAnswer == Answer.FAIL }
                val currentUser = userRepository.getCurrentUser()
                val now = java.time.Instant.now().toString()
                for (item in failedNonCriticalItems) {
                    try {
                        val answeredItemId = answeredItemIds.value[item.id]
                        if (answeredItemId == null) {
                            android.util.Log.e("ChecklistViewModel", "No answeredItemId found for item ${item.id}")
                            continue
                        }
                        val alert = app.forku.data.api.dto.safetyalert.SafetyAlertDto(
                            id = java.util.UUID.randomUUID().toString(),
                            answeredChecklistItemId = answeredItemId,
                            goUserId = currentUser?.id ?: "",
                            vehicleId = vehicleId.toString(),
                            isDirty = true,
                            isNew = true,
                            isMarkedForDeletion = false,
                            creationDateTime = now
                        )
                        val result = createSafetyAlertUseCase(alert)
                        android.util.Log.d("ChecklistViewModel", "SafetyAlert created for item ${item.id}: ${result?.id}")
                    } catch (e: Exception) {
                        android.util.Log.e("ChecklistViewModel", "Error creating SafetyAlert for item ${item.id}: ${e.message}", e)
                    }
                }

                // --- NEW: Request initial hour meter if the checklist is approved ---
                if (validation.status == CheckStatus.COMPLETED_PASS) {
                    android.util.Log.d("ChecklistViewModel", "Checklist is COMPLETED_PASS, showing hour meter dialog...")
                    _state.update { 
                        it?.copy(
                            showInitialHourMeterDialog = true,
                            pendingChecklistAnswerId = savedAnswer.id
                        ) 
                    }
                } else if (validation.status == CheckStatus.COMPLETED_FAIL) {
                    // Bloquear vehículo ya ocurre en repositorio, solo navega y muestra mensaje
                    _state.update { it?.copy(vehicleBlocked = true, message = "The vehicle has been blocked due to a checklist failure.") }
                    _navigationEvent.value = NavigationEvent.VehicleBlocked
                    // Cambiar estado del vehículo a OUT_OF_SERVICE
                    val vehicleId = state.value?.vehicleId
                    if (vehicleId != null) {
                        val businessId = businessContextManager.getCurrentBusinessId() ?: state.value?.vehicle?.businessId ?: ""
                        vehicleStatusUpdater.updateVehicleStatus(vehicleId, VehicleStatus.OUT_OF_SERVICE, businessId)
                    }
                }
            } catch (e: Exception) {
                _state.update { it?.copy(isLoading = false, error = e.message ?: "Failed to save checklist answer") }
            }
        }
    }

    private fun stopTimerIfCompleted() {
        val s = _state.value
        if (s?.isCompleted == true) {
            timerJob?.cancel()
            timerJob = null
            android.util.Log.d("ChecklistViewModel", "Timer stopped because checklist is completed.")
        }
    }

    fun updateItemComment(id: String, comment: String) {
        if (state.value?.isReadOnly == true) {
            android.util.Log.d("ChecklistFields", "Cannot update comment - Checklist is readonly")
            return
        }
        android.util.Log.d("ChecklistFields", "Updating comment for item[$id] - New comment: ${comment.take(20)}...")
        val currentItems = state.value?.checkItems?.toMutableList() ?: mutableListOf()
        val itemIndex = currentItems.indexOfFirst { it.id == id }
        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            currentItems[itemIndex] = item.copy(userComment = comment)
            _state.update { currentState ->
                android.util.Log.d("ChecklistFields", "State updated with new comment for item[$id]")
                currentState?.copy(
                    checkItems = currentItems,
                    hasUnsavedChanges = true
                )
            }
        } else {
            android.util.Log.w("ChecklistFields", "Item[$id] not found when trying to update comment")
        }
    }

    fun onAddImage(itemId: String, uri: Uri) {
        android.util.Log.d("ChecklistImage", "onAddImage called for item[$itemId] - URI: $uri")
        _itemImages.update { current ->
            val currentImages = current[itemId].orEmpty()
            android.util.Log.d("ChecklistImage", "Current images for item[$itemId]: ${currentImages.size}")
            val updated = currentImages + uri.toString()
            android.util.Log.d("ChecklistImage", "Updated images for item[$itemId] - Total images: ${updated.size}")
            current + (itemId to updated)
        }
    }

    fun onRemoveImage(itemId: String, uri: Uri) {
        android.util.Log.d("ChecklistImage", "onRemoveImage called for item[$itemId] - URI: $uri")
        _itemImages.update { current ->
            val currentImages = current[itemId].orEmpty()
            android.util.Log.d("ChecklistImage", "Current images before removal for item[$itemId]: ${currentImages.size}")
            val updated = currentImages.filterNot { it == uri.toString() }
            android.util.Log.d("ChecklistImage", "Updated images after removal for item[$itemId] - Remaining images: ${updated.size}")
            current + (itemId to updated)
        }
    }

    // Helper: Get the real AnsweredChecklistItem for a given checklist item
    private suspend fun getOrCreateAnsweredChecklistItemId(itemId: String): String {
        // 1. Buscar primero en el mapping de la UI
        answeredItemIds.value[itemId]?.let { return it }
        val checklistAnswerId = state.value?.checklistAnswerId ?: throw Exception("ChecklistAnswerId not found")
        // 2. Consultar el repositorio solo para el checklistAnswerId actual
        val answered = answeredChecklistItemRepository.getAll().find {
            it.checklistAnswerId == checklistAnswerId && it.checklistItemId == itemId
        }
        if (answered != null) {
            // Actualizar el mapping inmediatamente
            _answeredItemIds.update { it + (itemId to answered.id) }
            logAnsweredItemIdsState()
            return answered.id
        }
        // 3. Si aún no existe, crearlo, guardarlo y actualizar el mapping
        val item = state.value?.checkItems?.find { it.id == itemId } ?: throw Exception("ChecklistItem not found")
        val newAnsweredItem = AnsweredChecklistItem(
            id = java.util.UUID.randomUUID().toString(),
            checklistId = item.checklistId,
            checklistAnswerId = checklistAnswerId,
            checklistItemId = item.id,
            question = item.question,
            answer = item.userAnswer?.name ?: "",
            userId = userRepository.getCurrentUser()?.id ?: "",
            createdAt = java.time.Instant.now().toString(),
            isNew = true,
            isDirty = true,
            userComment = item.userComment
        )
        answeredChecklistItemRepository.save(newAnsweredItem)
        _answeredItemIds.update { it + (itemId to newAnsweredItem.id) }
        logAnsweredItemIdsState()
        return newAnsweredItem.id
    }

    fun uploadAndAttachImage(itemId: String, uri: Uri) {
        android.util.Log.d("ChecklistViewModel", "[ImageUpload][START] uploadAndAttachImage called for itemId=$itemId, uri=$uri")
        // Asegura que la imagen esté en la lista local para previsualización inmediata
        _itemImages.update { current ->
            val currentImages = current[itemId].orEmpty()
            if (!currentImages.contains(uri.toString())) {
                current + (itemId to (currentImages + uri.toString()))
            } else {
                current
            }
        }
        viewModelScope.launch {
            try {
                android.util.Log.d("appflow", "[ImageUpload][STEP1] Current answeredItemIds mapping: ${_answeredItemIds.value}")
                android.util.Log.d("appflow", "[ImageUpload][STEP1] Current uploadedMultimedia state: ${_uploadedMultimedia.value}")
                _uploadingImages.update { current ->
                    android.util.Log.d("appflow", "[ImageUpload][STEP2] Added uri to uploadingImages set - Current size: ${current.size}")
                    current + uri
                }
                android.util.Log.d("appflow", "[ImageUpload][STEP3] Converting URI to file for itemId=$itemId")
                val file = uriToFile(uri)
                android.util.Log.d("appflow", "[ImageUpload][STEP3] File created: ${file.name}, size=${file.length()}")
                android.util.Log.d("appflow", "[ImageUpload][STEP4] Uploading file for itemId=$itemId")
                val uploadResult = uploadFileUseCase.uploadImageFile(file)
                val uploadFile = uploadResult.getOrThrow()
                android.util.Log.d("appflow", "[ImageUpload][STEP4] File uploaded successfully: ${uploadFile.internalName}")
                android.util.Log.d("appflow", "[ImageUpload][STEP5] Getting or creating AnsweredChecklistItem for itemId=$itemId")
                val answeredChecklistItemId = getOrCreateAnsweredChecklistItemId(itemId)
                android.util.Log.d("appflow", "[ImageUpload][STEP5] AnsweredChecklistItemId=$answeredChecklistItemId")
                val goUserId = userRepository.getCurrentUser()?.id ?: ""
                android.util.Log.d("appflow", "[ImageUpload][STEP6] Building multimedia JSON for itemId=$itemId, goUserId=$goUserId")
                val json = buildChecklistItemAnswerMultimediaJson(answeredChecklistItemId, uploadFile, goUserId)
                android.util.Log.d("appflow", "[ImageUpload][STEP6] JSON built: $json")
                android.util.Log.d("appflow", "[ImageUpload][STEP7] Adding multimedia to checklist item")
                val multimediaResult = addChecklistItemAnswerMultimediaUseCase(json)
                val multimedia = multimediaResult.getOrThrow()
                android.util.Log.d("appflow", "[ImageUpload][STEP7] Multimedia added successfully: id=${multimedia.id}")
                android.util.Log.d("appflow", "[ImageUpload][STEP8] Fetching updated multimedia list for AnsweredChecklistItemId=$answeredChecklistItemId")
                val updatedMultimediaResult = getChecklistItemAnswerMultimediaByAnswerIdUseCase(answeredChecklistItemId)
                updatedMultimediaResult.onSuccess { multimediaList ->
                    android.util.Log.d("appflow", "[ImageUpload][STEP8] Updating _uploadedMultimedia for answeredChecklistItemId=$answeredChecklistItemId, multimediaList.size=${multimediaList.size}")
                    _uploadedMultimedia.update { current ->
                        val updated = current + (answeredChecklistItemId to multimediaList)
                        android.util.Log.d("appflow", "[ImageUpload][STEP8] Updated _uploadedMultimedia state: $updated")
                        updated
                    }
                    // Forzar actualización de _answeredItemIds para este itemId
                    _answeredItemIds.update { current ->
                        current + (itemId to answeredChecklistItemId)
                    }
                    // Eliminar la imagen local que ya fue subida
                    _itemImages.update { current ->
                        val updatedUris = current[itemId]?.filter { it != uri.toString() } ?: emptyList()
                        current + (itemId to updatedUris)
                    }
                }.onFailure { error ->
                    android.util.Log.e("appflow", "[ImageUpload][ERROR] Failed to fetch updated multimedia list: ${error.message}", error)
                }
                android.util.Log.d("appflow", "[ImageUpload][END] Image upload process completed successfully for itemId=$itemId")
            } catch (e: Exception) {
                android.util.Log.e("appflow", "[ImageUpload][ERROR] Error uploading image for itemId=$itemId: ${e.message}", e)
            } finally {
                _uploadingImages.update { current -> current - uri }
                _uploading.update { current -> current + (itemId to false) }
            }
        }
    }

    fun removeImageFromBackend(itemId: String, multimediaId: String) {
        android.util.Log.d("ChecklistImage", "Removing image from backend: itemId=$itemId, multimediaId=$multimediaId")
        viewModelScope.launch {
            try {
                deleteChecklistItemAnswerMultimediaUseCase(multimediaId)
                android.util.Log.d("ChecklistImage", "Successfully deleted multimedia: id=$multimediaId")
                _uploadedMultimedia.update { current ->
                    val updated = current[itemId].orEmpty().filterNot { it.id == multimediaId }
                    android.util.Log.d("ChecklistImage", "Updated multimedia state after removal: itemId=$itemId, remaining images=${updated.size}")
                    current + (itemId to updated)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChecklistImage", "Error removing image: ${e.message}", e)
            }
        }
    }

    // Utility: Convert Uri to File (implementation for Android)
    private fun uriToFile(uri: Uri): File {
        val contentResolver = appContext.contentResolver
        return if (uri.scheme == "file") {
            // Si es un archivo directo, úsalo
            val file = File(uri.path!!)
            android.util.Log.d("ImageDebug", "[uriToFile] file:// path: ${file.absolutePath}, size: ${file.length()}")
            file
        } else {
            // Si es content://, copia el contenido
            val fileName = getFileName(uri)
            val tempFile = File(appContext.externalCacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            android.util.Log.d("ImageDebug", "[uriToFile] Copied content:// to: ${tempFile.absolutePath}, size: ${tempFile.length()}")
            tempFile
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_image"
        val cursor = appContext.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = it.getString(index)
            }
        }
        return name
    }

    // Utility: Build JSON for ChecklistItemAnswerMultimedia (flat, no multimedia field)
    private fun buildChecklistItemAnswerMultimediaJson(itemId: String, uploadFile: Any, goUserId: String): String {
        android.util.Log.d("ImageUpload", "[JSON] Iniciando construcción de JSON para itemId=$itemId")
        val checklistAnswerId = state.value?.checklistAnswerId ?: ""
        android.util.Log.d("ImageUpload", "[JSON] checklistAnswerId=$checklistAnswerId, goUserId=$goUserId")
        val file = uploadFile as app.forku.domain.model.gogroup.UploadFile
        val now = java.time.Instant.now().toString()
        
        // Get business and site context from current state (synchronous)
        val businessContextState = businessContextManager.contextState.value
        val businessId = businessContextState.businessId
        val siteId = businessContextState.siteId
        
        val dto = ChecklistItemAnswerMultimediaDto(
            checklistItemAnswerId = itemId,
            description = "",
            createdAt = now,
            createdAtWithTimezoneOffset = now,
            creationDateTime = now,
            entityType = 2,
            goUserId = goUserId,
            image = file.internalName,
            imageInternalName = file.internalName,
            imageFileSize = file.fileSize.toInt(),
            multimediaType = 0,
            isNew = true,
            isDirty = true,
            isMarkedForDeletion = false,
            businessId = businessId,
            siteId = siteId
        )
        val json = gson.toJson(dto)
        android.util.Log.d("ImageUpload", "[JSON] JSON final generado: $json")
        return json
    }

    /**
     * Debug: Fetch and log all multimedia associated with a given AnsweredChecklistItemId
     */
    fun debugFetchAndLogMultimediaForAnswer(answeredChecklistItemId: String) {
        android.util.Log.d("ChecklistDebug", "Fetching multimedia for AnsweredChecklistItemId=$answeredChecklistItemId")
        viewModelScope.launch {
            val result = getChecklistItemAnswerMultimediaByAnswerIdUseCase(answeredChecklistItemId)
            result.onSuccess { multimediaList ->
                android.util.Log.d("ChecklistDebug", "Fetched ${multimediaList.size} multimedia items")
                multimediaList.forEachIndexed { index, multimedia ->
                    android.util.Log.d(
                        "ChecklistDebug",
                        "Multimedia[$index]: id=${multimedia.id}, " +
                        "imageInternalName=${multimedia.imageInternalName}, " +
                        "imageUrl=${multimedia.imageUrl}, " +
                        "fileSize=${multimedia.imageFileSize}, " +
                        "createdAt=${multimedia.createdAt}, " +
                        "description=${multimedia.description}"
                    )
                }
            }.onFailure { error ->
                android.util.Log.e("ChecklistDebug", "Failed to fetch multimedia: ${error.message}", error)
            }
        }
    }

    suspend fun getAuthHeaders(): Pair<String, String> {
        val headers = headerManager.getHeaders().getOrThrow()
        return headers.csrfToken to headers.cookie
    }

    suspend fun getAuthHeadersFull(): app.forku.core.auth.HeaderManager.Headers {
        return headerManager.getHeaders().getOrThrow()
    }

    private fun logUploadedMultimediaState() {
        val summary = _uploadedMultimedia.value.map { (k, v) -> "$k: ${v.size} items" }
        android.util.Log.d("ChecklistImage", "[GLOBAL] _uploadedMultimedia keys: $summary")
    }
    private fun logAnsweredItemIdsState(context: String = "") {
        val mapping = _answeredItemIds.value
        val state = state.value
        android.util.Log.d("appflow", "[Mapping][$context] answeredItemIds: $mapping | checklistId=${state?.checklistId}, checklistAnswerId=${state?.checklistAnswerId}")
    }

    // 5. When rendering or using answeredItemId (add a helper function to log this in the ViewModel and call it from the UI if possible):
    fun logAnsweredItemIdUsage(itemId: String, answeredItemId: String?) {
        android.util.Log.d("appflow", "[UI] Using answeredItemId=$answeredItemId for itemId=$itemId")
    }

    // 2. Loggear cada vez que se crea o actualiza un AnsweredChecklistItem
    private fun logAnsweredChecklistItemEvent(event: String, item: ChecklistItem, answeredItem: AnsweredChecklistItem) {
        android.util.Log.d("appflow", "[AnsweredChecklistItem][$event] itemId=${item.id}, checklistId=${item.checklistId}, checklistAnswerId=${answeredItem.checklistAnswerId}, answeredChecklistItemId=${answeredItem.id}, userId=${answeredItem.userId}")
    }

    // 3. Loggear cada vez que se sube una imagen
    private fun logImageUploadEvent(itemId: String, checklistId: String?, checklistAnswerId: String?, answeredChecklistItemId: String?) {
        android.util.Log.d("appflow", "[ImageUpload] itemId=$itemId, checklistId=$checklistId, checklistAnswerId=$checklistAnswerId, answeredChecklistItemId=$answeredChecklistItemId")
    }

    fun createTempPhotoUri(context: Context): Uri? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "PHOTO_${System.currentTimeMillis()}_.jpg"
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ForkU")
            }
            val uri = contentResolver.insert(imageCollection, contentValues)
            _tempPhotoUri.value = uri
            uri
        } catch (e: Exception) {
            android.util.Log.e("ChecklistViewModel", "Error creating temp photo URI (MediaStore): ${e.message}", e)
            null
        }
    }
}

sealed class NavigationEvent {
    object Back : NavigationEvent()
    data class AfterSubmit(val role: String) : NavigationEvent()
    object VehicleBlocked : NavigationEvent()
}