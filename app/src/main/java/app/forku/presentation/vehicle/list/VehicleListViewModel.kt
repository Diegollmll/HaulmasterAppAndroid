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
        
    }

    fun loadVehicles(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                ) }
                
                val vehicles = getVehiclesUseCase()
                _state.update { 
                    it.copy(
                        vehicles = vehicles,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Unknown error, failed to load vehicles",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }
} 