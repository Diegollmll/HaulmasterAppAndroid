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


@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val getVehicleActiveSessionUseCase: GetVehicleActiveSessionUseCase,
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val getVehicleStatusUseCase: GetVehicleStatusUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleProfileState())
    val state = _state.asStateFlow()

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    init {
        loadVehicle()
    }

    fun loadVehicle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val vehicle = getVehicleUseCase(vehicleId)
                val activeSession = getVehicleActiveSessionUseCase(vehicleId)
                val lastPreShiftCheck = vehicleRepository.getLastPreShiftCheck(vehicleId)
                
                _state.update {
                    it.copy(
                        vehicle = vehicle,
                        activeSession = activeSession,
                        hasActivePreShiftCheck = lastPreShiftCheck?.status == PreShiftStatus.IN_PROGRESS.toString(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to load vehicle",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleQrCode() {
        _state.update { it.copy(showQrCode = !it.showQrCode) }
    }

    fun startVehicleSession(checkId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val session = sessionRepository.startSession(vehicleId, checkId)
                _state.update { 
                    it.copy(
                        hasActiveSession = session.status == SessionStatus.ACTIVE,
                        isLoading = false,
                        error = null
                    )
                }
                loadVehicle() // Refresh vehicle state
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}