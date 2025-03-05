package app.forku.presentation.vehicle.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.model.session.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.usecase.session.GetVehicleActiveSessionUseCase
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.checklist.ChecklistRepository


@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getVehicleActiveSessionUseCase: GetVehicleActiveSessionUseCase,
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    private val checklistRepository: ChecklistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleProfileState())
    val state = _state.asStateFlow()

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    init {
        loadVehicle(showLoading = true)
    }

    fun loadVehicle(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) {
                _state.update { it.copy(isLoading = true) }
            }
            try {
                val vehicle = getVehicleUseCase(vehicleId)
                val activeSession = getVehicleActiveSessionUseCase(vehicleId)
                val lastPreShiftCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                
                _state.update {
                    it.copy(
                        vehicle = vehicle,
                        activeSession = activeSession,
                        activeOperator = activeSession?.operator,
                        hasActivePreShiftCheck = lastPreShiftCheck?.status == PreShiftStatus.IN_PROGRESS.toString(),
                        hasActiveSession = activeSession?.session?.status == SessionStatus.ACTIVE,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Error al cargar vehículo",
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

    fun startSessionFromCheck() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
                
                if (lastCheck?.status == PreShiftStatus.COMPLETED_PASS.toString()) {
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

}