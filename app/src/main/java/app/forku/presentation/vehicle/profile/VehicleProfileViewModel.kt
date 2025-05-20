package app.forku.presentation.vehicle.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.user.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.vehicle.GetVehicleActiveSessionUseCase
import app.forku.presentation.vehicle.components.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream
import retrofit2.HttpException
import java.net.SocketTimeoutException
import app.forku.domain.model.session.VehicleSessionClosedMethod
import app.forku.domain.model.vehicle.toDisplayString
import android.util.Log
import app.forku.core.Constants
import app.forku.domain.repository.checklist.ChecklistAnswerRepository

@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getVehicleActiveSessionUseCase: GetVehicleActiveSessionUseCase,
    private val vehicleRepository: VehicleRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleProfileState())
    val state = _state.asStateFlow()

    private val vehicleId: String? = savedStateHandle.get<String>("vehicleId")
    private val navBusinessId: String? = savedStateHandle.get<String>("businessId") // Get businessId from navigation args

    init {
        Log.d("VehicleProfileVM", "Initializing for vehicleId: $vehicleId, navBusinessId: $navBusinessId")
        loadVehicle(showLoading = true)
    }

    private suspend fun <T> retryOnFailure(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 5000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxAttempts - 1) throw e
                
                when (e) {
                    is HttpException -> {
                        if (e.code() == 429) { // Too Many Requests
                            delay(currentDelay)
                            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                        } else throw e
                    }
                    is SocketTimeoutException -> {
                        delay(currentDelay)
                        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                    }
                    else -> throw e
                }
            }
        }
        throw IllegalStateException("Should never reach here")
    }

    fun loadVehicle(showLoading: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = showLoading) }
                
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update { it.copy(isLoading = false, error = "User not authenticated") }
                    return@launch
                }
                Log.d("VehicleProfileVM_Debug", "User details before determining effectiveBusinessId: Role=${currentUser.role}, UserBusinessId=${currentUser.businessId}, NavBusinessId=$navBusinessId")
                
                val userRole = currentUser.role
                _state.update { it.copy(currentUserRole = userRole) }

                // Determine the businessId to use for fetching based on role and nav args
                val effectiveBusinessId = when (userRole) {
                    UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> navBusinessId ?: Constants.BUSINESS_ID
                    else -> currentUser.businessId ?: Constants.BUSINESS_ID
                }
                Log.d("VehicleProfileVM_Debug", "Determined effectiveBusinessId: $effectiveBusinessId based on Role=$userRole")

                Log.d("VehicleProfileVM", "Attempting to load vehicle $vehicleId with effective businessId $effectiveBusinessId")
                val vehicle = retryOnFailure {
                    vehicleRepository.getVehicle(vehicleId!!, effectiveBusinessId)
                }
                
                if (vehicle == null) {
                    throw Exception("Error al cargar vehiculo")
                }
                
                // Load other related data (session, checks) using the actual vehicle.businessId
                val actualBusinessId = vehicle.businessId
                if (actualBusinessId != null) {
                    loadActiveSession(vehicle.id, actualBusinessId)
                    loadLastPreShiftCheck(vehicle.id, actualBusinessId)
                    val lastChecklistAnswer = checklistAnswerRepository.getLastChecklistAnswerForVehicle(vehicle.id)
                    val lastChecklistOperator = lastChecklistAnswer?.goUserId?.takeIf { it.isNotBlank() }?.let { userRepository.getUserById(it) }
                    // Fetch last operator if no active session
                    val lastSession = vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicle.id)
                    android.util.Log.d("VehicleProfileVM", "lastSession: ${lastSession?.id}, userId: ${lastSession?.userId}")
                    val lastOperator = lastSession?.userId?.let { 
                        val user = userRepository.getUserById(it)
                        Log.d("VehicleProfileVM", "Fetched lastOperator: id=${user?.id}, firstName=${user?.firstName}, lastName=${user?.lastName}, username=${user?.username}, photoUrl=${user?.photoUrl}")
                        user
                    }
                    android.util.Log.d("VehicleProfileVM", "lastOperator: ${lastOperator?.id} - ${lastOperator?.fullName}")
                    _state.update {
                        it.copy(
                            lastChecklistAnswer = lastChecklistAnswer,
                            lastChecklistOperator = lastChecklistOperator,
                            lastOperator = lastOperator
                        )
                    }
                } else {
                    Log.w("VehicleProfileVM", "Vehicle ${vehicle.id} has no businessId, skipping session/check load.")
                }
                
                _state.update { 
                    it.copy(
                        vehicle = vehicle,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("VehicleProfileVM", "Error loading vehicle $vehicleId", e)
                _state.update {
                    it.copy(
                        error = when (e) {
                            is HttpException -> when (e.code()) {
                                429 -> "Too many requests. Please try again later."
                                else -> "Network error: ${e.message()}"
                            }
                            is SocketTimeoutException -> "Connection timeout. Please check your internet connection."
                            else -> "Error: ${e.message}"
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadActiveSession(vehicleId: String, businessId: String) {
        try {
            val session = vehicleSessionRepository.getActiveSessionForVehicle(vehicleId, businessId)
            
            // Fetch operator details if there's an active session
            val operator = session?.userId?.let { userId ->
                retryOnFailure {
                    userRepository.getUserById(userId)
                }
            }

            // If no active operator, get the last session's operator
            val lastOperator = if (operator == null) {
                withTimeoutOrNull(5000) { // Add timeout to prevent hanging
                    vehicleSessionRepository.getLastCompletedSessionForVehicle(vehicleId)?.userId?.let { userId ->
                        retryOnFailure {
                            userRepository.getUserById(userId)
                        }
                    }
                }
            } else null
            
            _state.update { 
                it.copy(
                    activeSession = session,
                    hasActiveSession = session != null,
                    activeOperator = operator,
                    lastOperator = lastOperator
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Error loading active session: ${e.message}") }
        }
    }

    private suspend fun loadLastPreShiftCheck(vehicleId: String, businessId: String) {
        try {
            val check = checklistRepository.getLastPreShiftCheck(vehicleId, businessId)
            
            _state.update { 
                it.copy(
                    hasActivePreShiftCheck = check?.status == CheckStatus.IN_PROGRESS.toString()
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Error loading last pre-shift check: ${e.message}") }
        }
    }

    fun refresh() {
        loadVehicle(showLoading = false)
    }

    fun refreshWithLoading() {
        loadVehicle(showLoading = true)
    }

    fun toggleQrCode() {
        viewModelScope.launch {
            try {
                val currentUser = userRepository.getCurrentUser()
                if (currentUser?.role == UserRole.ADMIN) {
                    _state.update { it.copy(showQrCode = !it.showQrCode) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error toggling QR code: ${e.message}") }
            }
        }
    }

    fun shareQrCode() {
        viewModelScope.launch {
            try {
                state.value.vehicle?.let { vehicle ->
                    // Generate QR code bitmap
                    val qrBitmap = QrCodeGenerator.generateVehicleQrCode(vehicle.id)
                    
                    // Save bitmap to temporary file
                    val cachePath = File(context.cacheDir, "qr_codes")
                    cachePath.mkdirs()
                    
                    val file = File(cachePath, "vehicle_qr_${vehicle.id}.png")
                    FileOutputStream(file).use { out ->
                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    
                    // Get content URI using FileProvider
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    
                    // Create share intent
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "Vehicle QR Code")
                        putExtra(Intent.EXTRA_TEXT, "Scan this QR code to access vehicle information")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    // Start share activity
                    val chooserIntent = Intent.createChooser(shareIntent, "Share QR Code")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooserIntent)
                }
            } catch (e: Exception) {
                // Handle error
                _state.update { it.copy(error = "Error sharing QR code: ${e.message}") }
            }
        }
    }

    fun startSessionFromCheck() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Get current user and business context
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId ?: Constants.BUSINESS_ID
                
                if (businessId == null) {
                    _state.update { 
                        it.copy(
                            error = "No business context available",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Check vehicle status first
                val vehicleStatus = getVehicleStatusUseCase(vehicleId!!)
                if (!vehicleStatus.isAvailable()) {
                    throw Exception(vehicleStatus.getErrorMessage())
                }
                
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId!!, businessId)
                
                if (lastCheck?.status == CheckStatus.COMPLETED_PASS.toString()) {
                    val session = vehicleSessionRepository.startSession(
                        vehicleId = vehicleId!!,
                        checkId = lastCheck.id
                    )
                    
                    // Reload vehicle state after starting session
                    loadVehicle()
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al iniciar sesión: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun startCheckForVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val canStartCheck = checklistRepository.canStartCheck(vehicleId)
                if (!canStartCheck) {
                    _state.update { 
                        it.copy(
                            error = "Cannot start check - Vehicle is currently in use",
                            isLoading = false,
                            canStartCheck = false
                        )
                    }
                    return@launch
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        navigateToChecklist = true,
                        canStartCheck = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false,
                        canStartCheck = false
                    )
                }
            }
        }
    }

    suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck? {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser == null) {
            Log.e("VehicleProfile", "User not authenticated for getLastPreShiftCheck")
            return null
        }

        // Determine the correct business context for fetching the check
        val vehicleFromState = _state.value.vehicle // Get the already loaded vehicle
        val effectiveBusinessId = when (currentUser.role) {
            UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> vehicleFromState?.businessId ?: "0" // Use vehicle's ID or placeholder '0'
            else -> currentUser.businessId // Use user's business ID for non-admins
        }

        if (effectiveBusinessId == null && currentUser.role != UserRole.SYSTEM_OWNER && currentUser.role != UserRole.SUPERADMIN) {
            Log.e("VehicleProfile", "No business context available for user to get last check")
            return null
        }

        // Use the determined effectiveBusinessId (could be null for admin/global if vehicle isn't assigned)
        // The repository needs to handle null/"0" appropriately for global checks if applicable.
        // Assuming getLastPreShiftCheck in repository handles '0' or requires a valid ID.
        // If '0' isn't valid for checks, we might need to skip if vehicleFromState?.businessId is null.
        val checkBusinessId = effectiveBusinessId ?: run {
            Log.w("VehicleProfile", "Vehicle has no businessId, cannot fetch specific check. Need global check logic?")
            // Decide if you want to attempt a global fetch (e.g., with "0") or return null
            return null // Returning null for now if vehicle has no business ID
        }

        Log.d("VehicleProfile", "Fetching last check for vehicle $vehicleId with business context: $checkBusinessId")
        return try {
            checklistRepository.getLastPreShiftCheck(vehicleId, checkBusinessId)
        } catch (e: Exception) {
            Log.e("VehicleProfile", "Error fetching last check for vehicle $vehicleId, business $checkBusinessId", e)
            null // Return null on error
        }
    }

    fun endVehicleSession() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId ?: Constants.BUSINESS_ID
                
                if (businessId == null) {
                    _state.update { 
                        it.copy(
                            error = "No business context available",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                if (currentUser?.role != UserRole.ADMIN) {
                    _state.update { 
                        it.copy(
                            error = "Only administrators can end sessions",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val session = state.value.activeSession ?: run {
                    _state.update { 
                        it.copy(
                            error = "No active session to end",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // End the session
                vehicleSessionRepository.endSession(
                    sessionId = session.id,
                    closeMethod = VehicleSessionClosedMethod.ADMIN_CLOSED,
                    adminId = currentUser.id,
                    notes = "Session ended by administrator"
                )

                // Update vehicle status to AVAILABLE
                state.value.vehicle?.id?.let { vehicleId ->
                    vehicleRepository.updateVehicleStatus(vehicleId, VehicleStatus.AVAILABLE, businessId)
                }
                
                // Reload vehicle state to reflect all changes
                loadVehicle(showLoading = false)
                
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error ending session: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateVehicleStatus(newStatus: VehicleStatus) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val currentUser = userRepository.getCurrentUser()
                val businessId = currentUser?.businessId ?: Constants.BUSINESS_ID
                
                if (businessId == null) {
                    _state.update { 
                        it.copy(
                            error = "No business context available",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                if (currentUser?.role != UserRole.ADMIN) {
                    _state.update { 
                        it.copy(
                            error = "Only administrators can change vehicle status",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                val vehicleId = state.value.vehicle?.id ?: return@launch
                val currentStatus = state.value.vehicle?.status

                // Don't proceed if trying to set the same status
                if (currentStatus == newStatus) {
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                // If there's an active session and we're changing status, end it first
                state.value.activeSession?.let { session ->
                    vehicleSessionRepository.endSession(
                        sessionId = session.id,
                        closeMethod = VehicleSessionClosedMethod.ADMIN_CLOSED,
                        adminId = currentUser.id,
                        notes = "Session ended due to vehicle status change from ${currentStatus?.toDisplayString()} to ${newStatus.toDisplayString()}"
                    )
                }

                // Update vehicle status
                vehicleRepository.updateVehicleStatus(vehicleId, newStatus, businessId)
                
                // Reload vehicle state to reflect changes
                loadVehicle(showLoading = false)
                
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error updating vehicle status: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}