package app.forku.presentation.vehicle.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.model.session.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.vehicle.GetVehicleActiveSessionUseCase
import app.forku.presentation.vehicle.components.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream

@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getVehicleActiveSessionUseCase: GetVehicleActiveSessionUseCase,
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleProfileState())
    val state = _state.asStateFlow()

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    init {
        loadVehicle(showLoading = true)
    }

    fun loadVehicle(showLoading: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = showLoading) }
                
                // Get any active session for the user (regardless of vehicle)
                val currentSession = sessionRepository.getCurrentSession()
                
                // Get vehicle details
                val vehicle = vehicleRepository.getVehicle(vehicleId)
                
                if (vehicle == null) {
                    android.util.Log.e("appflow VehicleProfile", "Vehicle not found for ID: $vehicleId")
                    throw Exception("Error al cargar vehiculo")
                }
                
                android.util.Log.d("appflow VehicleProfile", "Vehicle loaded successfully: ${vehicle.id}")
                
                // Get active session for this vehicle
                val activeSession = sessionRepository.getActiveSessionForVehicle(vehicleId)
                android.util.Log.d("appflow VehicleProfile", "Active session: ${activeSession?.id}")
                
                // Get last pre-shift check
                val lastPreShiftCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                android.util.Log.d("appflow VehicleProfile", "Last pre-shift check: ${lastPreShiftCheck?.id}")
                
                // Fetch operator details if there's an active session
                val operator = activeSession?.userId?.let { userId ->
                    userRepository.getUserById(userId)
                }

                // If no active operator, get the last session's operator
                val lastOperator = if (operator == null) {
                    sessionRepository.getLastCompletedSessionForVehicle(vehicleId)?.userId?.let { userId ->
                        userRepository.getUserById(userId)
                    }
                } else null
                
                _state.update { 
                    it.copy(
                        vehicle = vehicle,
                        activeSession = activeSession,
                        hasActiveSession = currentSession != null,
                        hasActivePreShiftCheck = lastPreShiftCheck?.status == CheckStatus.IN_PROGRESS.toString(),
                        activeOperator = operator,
                        lastOperator = lastOperator,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("appflow VehicleProfile", "Error loading vehicle", e)
                _state.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        loadVehicle(showLoading = false)
    }

    fun refreshWithLoading() {
        loadVehicle(showLoading = true)
    }

    fun toggleQrCode() {
        _state.update { it.copy(showQrCode = !it.showQrCode) }
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
                
                // Check vehicle status first
                val vehicleStatus = getVehicleStatusUseCase(vehicleId)
                if (!vehicleStatus.isAvailable()) {
                    throw Exception(vehicleStatus.getErrorMessage())
                }
                
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                
                if (lastCheck?.status == CheckStatus.COMPLETED_PASS.toString()) {
                    val session = sessionRepository.startSession(
                        vehicleId = vehicleId,
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
        return checklistRepository.getLastPreShiftCheck(vehicleId)
    }
}