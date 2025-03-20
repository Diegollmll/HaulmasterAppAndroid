package app.forku.presentation.scanner

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    
    override fun onCleared() {
        super.onCleared()
        cameraProvider?.unbindAll()
        cameraProvider = null
    }

    fun onQrScanned(code: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}
