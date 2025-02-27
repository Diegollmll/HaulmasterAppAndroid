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
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val getVehicleUseCase: GetVehicleUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    
    init {
        loadDashboard()
    }
    
    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = "Please login to view dashboard"
                        )
                    }
                    return@launch
                }

                // Get current session and associated vehicle
                val currentSession = sessionRepository.getCurrentSession()
                val activeVehicle = currentSession?.let {
                    try {
                        getVehicleUseCase(it.vehicleId)
                    } catch (e: Exception) {
                        android.util.Log.e("Dashboard", "Error loading active vehicle", e)
                        null
                    }
                }

                // Get vehicle status and last check
                val vehicleStatus = activeVehicle?.let { 
                    getVehicleStatusUseCase(it.id) 
                } ?: VehicleStatus.UNKNOWN
                
                val lastCheck = activeVehicle?.let { 
                    vehicleRepository.getLastPreShiftCheck(it.id)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        user = currentUser,
                        isAuthenticated = true,
                        lastPreShiftCheck = lastCheck,
                        vehicleStatus = vehicleStatus,
                        currentSession = currentSession,
                        activeVehicle = activeVehicle,
                        error = null
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

    fun refreshDashboard() {
        loadDashboard()
    }
} 