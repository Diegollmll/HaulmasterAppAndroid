package app.forku.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckCurrentUserUseCase
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
    private val checklistRepository: ChecklistRepository,
    private val authRepository: AuthRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getLastPreShiftCheckCurrentUserUseCase: GetLastPreShiftCheckCurrentUserUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            loadDashboard(showLoading = true)
        }
    }
    
    private suspend fun loadDashboard(showLoading: Boolean = false) {
        try {
            if (showLoading) {
                _state.update { it.copy(isLoading = true) }
            }
            
            val currentUser = authRepository.getCurrentUser()
                ?: throw Exception("User not authenticated")

            val currentSession = sessionRepository.getCurrentSession()
            val activeVehicle = currentSession?.let { getVehicleUseCase(it.vehicleId) }
            val vehicleStatus = activeVehicle?.let { getVehicleStatusUseCase(it.id) }
                ?: VehicleStatus.UNKNOWN

            _state.update {
                it.copy(
                    isLoading = false,
                    user = currentUser,
                    isAuthenticated = true,
                    lastPreShiftCheck = getLastPreShiftCheckCurrentUserUseCase(),
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
                    error = "Error al cargar dashboard: ${e.message}"
                )
            }
        }
    }

    // Refresh silencioso (sin loading) cuando volvemos a la pantalla
    fun refresh() {
        viewModelScope.launch {
            loadDashboard(showLoading = false)
        }
    }

    // Refresh con loading para pull-to-refresh o acciones explícitas del usuario
    fun refreshWithLoading() {
        viewModelScope.launch {
            loadDashboard(showLoading = true)
        }
    }
} 