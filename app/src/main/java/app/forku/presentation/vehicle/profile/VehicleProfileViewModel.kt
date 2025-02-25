package app.forku.presentation.vehicle.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val getVehicleUseCase: GetVehicleUseCase,
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
                _state.update {
                    it.copy(
                        vehicle = vehicle,
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