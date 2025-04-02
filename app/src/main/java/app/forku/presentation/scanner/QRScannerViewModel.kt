package app.forku.presentation.scanner

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
            return
        }

        viewModelScope.launch {
            try {
                isProcessingQR = true
                lastScannedCode = code
                _state.update { it.copy(isLoading = true) }
                
                // Get vehicle without availability check first
                val vehicle = vehicleRepository.getVehicleByQr(code, checkAvailability = false)
                
                // Check if vehicle can start check and user has no active session
                val canStartCheck = checklistRepository.canStartCheck(vehicle.id)
                val currentSession = vehicleSessionRepository.getCurrentSession()
                
                val shouldNavigateToChecklist = canStartCheck && currentSession == null
                
                _state.update { 
                    it.copy(
                        vehicle = vehicle,
                        isLoading = false,
                        canStartCheck = canStartCheck,
                        navigateToChecklist = shouldNavigateToChecklist,
                        navigateToProfile = !shouldNavigateToChecklist // Only navigate to profile if not going to checklist
                    )
                }

                // Reset the scanning lock after cooldown
                delay(scanCooldown)
                isProcessingQR = false
                lastScannedCode = null
            } catch (e: Exception) {
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
