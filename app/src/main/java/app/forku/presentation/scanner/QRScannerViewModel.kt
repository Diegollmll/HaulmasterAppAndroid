package app.forku.presentation.scanner

import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.domain.model.vehicle.VehicleStatus


@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val checklistRepository: ChecklistRepository,
    private val vehicleSessionRepository: VehicleSessionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QRScannerState())
    val state = _state.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var isProcessingQR = false
    private var lastScannedCode: String? = null
    private val scanCooldown = 2000L // 2 seconds cooldown between scans
    
    override fun onCleared() {
        super.onCleared()
        cameraProvider?.unbindAll()
        cameraProvider = null
    }

    fun onQrScanned(code: String) {
        // If we're already processing a QR or this is the same code within cooldown, ignore
        if (isProcessingQR || code == lastScannedCode) {
            Log.d("QRFlow", "Ignoring QR scan: isProcessingQR=$isProcessingQR, lastScannedCode=$lastScannedCode, code=$code")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("QRFlow", "[onQrScanned] Starting QR processing for code: $code")
                isProcessingQR = true
                lastScannedCode = code
                _state.update { it.copy(isLoading = true) }
                
                // Get vehicle without availability check first
                Log.d("QRFlow", "[onQrScanned] Calling getVehicleByQr with code: $code")
                val vehicle = vehicleRepository.getVehicleByQr(code, checkAvailability = false)
                Log.d("QRFlow", "[onQrScanned] Vehicle retrieved: id=${vehicle.id}, codename=${vehicle.codename}, type=${vehicle.type.Name}, status=${vehicle.status}")
                
                // VALIDACIÓN: Solo permitir si el vehículo está AVAILABLE
                if (vehicle.status != VehicleStatus.AVAILABLE) {
                    _state.update {
                        it.copy(
                            vehicle = vehicle,
                            isLoading = false,
                            canStartCheck = false,
                            navigateToChecklist = false,
                            navigateToProfile = false,
                            error = "Vehicle is not available for checklist. Current status: ${vehicle.status}"
                        )
                    }
                    isProcessingQR = false
                    lastScannedCode = null
                    return@launch
                }
                
                // Check if vehicle can start check and user has no active session
                Log.d("QRFlow", "[onQrScanned] Checking if vehicle can start check: vehicleId=${vehicle.id}")
                val canStartCheck = checklistRepository.canStartCheck(vehicle.id)
                Log.d("QRFlow", "[onQrScanned] canStartCheck result: $canStartCheck")
                
                Log.d("QRFlow", "[onQrScanned] Getting current session")
                val currentSession = vehicleSessionRepository.getCurrentSession()
                Log.d("QRFlow", "[onQrScanned] Current session: ${currentSession?.id ?: "null"}")
                
                // Check if the current session is for the same vehicle
                val isSameVehicle = currentSession?.vehicleId == vehicle.id
                Log.d("QRFlow", "[onQrScanned] Is same vehicle as current session: $isSameVehicle")
                
                val shouldNavigateToChecklist = canStartCheck && (currentSession == null || isSameVehicle)
                Log.d("QRFlow", "[onQrScanned] Navigation decision - shouldNavigateToChecklist: $shouldNavigateToChecklist")
                
                _state.update { 
                    it.copy(
                        vehicle = vehicle,
                        isLoading = false,
                        canStartCheck = canStartCheck,
                        navigateToChecklist = shouldNavigateToChecklist,
                        navigateToProfile = !shouldNavigateToChecklist,
                        error = if (!shouldNavigateToChecklist && currentSession != null && !isSameVehicle) {
                            "You already have an active session with another vehicle"
                        } else null
                    )
                }
                Log.d("QRFlow", "[onQrScanned] State updated with vehicle and navigation flags")

                // Reset the scanning lock after cooldown
                delay(scanCooldown)
                isProcessingQR = false
                lastScannedCode = null
                Log.d("QRFlow", "[onQrScanned] QR processing completed and cooldown finished")
            } catch (e: Exception) {
                Log.e("QRFlow", "[onQrScanned] Error processing QR: $code", e)
                Log.e("QRFlow", "[onQrScanned] Error details: ${e.message}")
                Log.e("QRFlow", "[onQrScanned] Stack trace: ${e.stackTraceToString()}")
                _state.update { 
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
                    )
                }
                isProcessingQR = false
                lastScannedCode = null
            }
        }
    }

    fun resetNavigationState() {
        _state.update { 
            it.copy(
                vehicle = null,
                navigateToChecklist = false,
                navigateToProfile = false,
                canStartCheck = false,
                isLoading = false,
                error = null
            )
        }
        isProcessingQR = false
        lastScannedCode = null
    }
}
