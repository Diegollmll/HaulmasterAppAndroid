package app.forku.presentation.vehicle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.usecase.vehicle.GetVehiclesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleListViewModel @Inject constructor(
    private val getVehiclesUseCase: GetVehiclesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(VehicleListState())
    val state = _state.asStateFlow()

    init {
        loadVehicles()
    }

    fun loadVehicles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val vehicles = getVehiclesUseCase()
                _state.update { 
                    it.copy(
                        vehicles = vehicles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to load vehicles",
                        isLoading = false
                    )
                }
            }
        }
    }
} 