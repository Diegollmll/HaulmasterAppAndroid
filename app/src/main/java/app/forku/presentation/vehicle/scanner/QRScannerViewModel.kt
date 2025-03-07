package app.forku.presentation.vehicle.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(QRScannerState())
    val state = _state.asStateFlow()

    sealed class NavigationEvent {
        data class ToPreShiftCheck(val vehicleId: String) : NavigationEvent()
        data class ToVehicleProfile(val vehicleId: String) : NavigationEvent()
        object None : NavigationEvent()
    }

    private val _navigation = MutableStateFlow<NavigationEvent>(NavigationEvent.None)
    val navigation = _navigation.asStateFlow()

    fun onQRScanned(vehicleId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val vehicleStatus = getVehicleStatusUseCase(vehicleId)
                
                if (vehicleStatus.isAvailable()) {
                    _navigation.value = NavigationEvent.ToPreShiftCheck(vehicleId)
                } else {
                    _state.update { 
                        it.copy(
                            error = vehicleStatus.getErrorMessage(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Error scanning QR code: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun resetNavigation() {
        _navigation.value = NavigationEvent.None
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class QRScannerState(
    val isLoading: Boolean = false,
    val error: String? = null
)