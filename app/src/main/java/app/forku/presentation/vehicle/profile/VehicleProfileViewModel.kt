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

@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
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
                val currentSession = sessionRepository.getCurrentSession()
                val lastPreShiftCheck = vehicleRepository.getLastPreShiftCheck()
                
                _state.update {
                    it.copy(
                        vehicle = vehicle,
                        hasActiveSession = currentSession?.status == SessionStatus.ACTIVE,
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

    fun toggleQrCode(show: Boolean) {
        if (!show) {
            _state.update { it.copy(showQrCode = false) }
        } else if (!state.value.showQrCode) {
            _state.update { it.copy(showQrCode = true) }
        }
    }
}