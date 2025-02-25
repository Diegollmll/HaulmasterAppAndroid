package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    
    init {
        loadDashboardStatus()
    }
    
    fun loadDashboardStatus() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Obtener el último check y el estado actual
                val lastCheck = vehicleRepository.getLastPreShiftCheck()
                val status = getVehicleStatusUseCase()
                
                _state.update { 
                    it.copy(
                        lastPreShiftCheck = lastCheck,
                        vehicleStatus = status,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to load status",
                        isLoading = false
                    )
                }
            }
        }
    }
} 