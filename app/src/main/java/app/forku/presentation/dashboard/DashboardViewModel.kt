package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.session.SessionRepository
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
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase
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
                
                val currentUser = try {
                    authRepository.getCurrentUser()
                } catch (e: Exception) {
                    null
                }
                
                val currentSession = try {
                    sessionRepository.getCurrentSession()
                } catch (e: Exception) {
                    null
                }
                
                val lastPreShiftCheck = try {
                    vehicleRepository.getLastPreShiftCheck()
                } catch (e: Exception) {
                    null
                }
                
                val vehicleStatus = try {
                    getVehicleStatusUseCase()
                } catch (e: Exception) {
                    VehicleStatus.CHECKED_OUT
                }
                
                val activeVehicle = try {
                    when (vehicleStatus) {
                        VehicleStatus.IN_USE -> currentSession?.vehicleId?.let { vehicleId -> 
                            vehicleRepository.getVehicleByQr(vehicleId)
                        }
                        VehicleStatus.CHECKED_IN -> lastPreShiftCheck?.vehicleId?.let { vehicleId -> 
                            vehicleRepository.getVehicleByQr(vehicleId)
                        }
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        vehicle = activeVehicle,
                        user = currentUser,
                        lastPreShiftCheck = lastPreShiftCheck,
                        isAuthenticated = currentUser != null,
                        showQrScanner = false,
                        vehicleStatus = vehicleStatus
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error loading dashboard: ${e.message}"
                    )
                }
            }
        }
    }
} 