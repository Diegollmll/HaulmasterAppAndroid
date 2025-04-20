package app.forku.presentation.system

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.api.dto.EnergySourceDto
import app.forku.domain.repository.energysource.EnergySourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnergySourceState(
    val energySources: List<EnergySourceDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EnergySourceViewModel @Inject constructor(
    private val repository: EnergySourceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EnergySourceState())
    val state: StateFlow<EnergySourceState> = _state.asStateFlow()

    init {
        loadEnergySources()
    }

    fun loadEnergySources() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.getAllEnergySources()
                if (response.isSuccessful) {
                    val sources = response.body() ?: emptyList()
                    _state.update { 
                        it.copy(
                            energySources = sources,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load energy sources: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EnergySourceVM", "Error loading energy sources", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error loading energy sources: ${e.message}"
                    )
                }
            }
        }
    }

    fun createEnergySource(name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val energySource = EnergySourceDto(
                    id = null,
                    name = name
                )
                val response = repository.createEnergySource(energySource)
                if (response.isSuccessful) {
                    loadEnergySources()
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to create energy source: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EnergySourceVM", "Error creating energy source", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error creating energy source: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateEnergySource(id: String, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val energySource = EnergySourceDto(
                    id = id,
                    name = name
                )
                val response = repository.updateEnergySource(id, energySource)
                if (response.isSuccessful) {
                    loadEnergySources()
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to update energy source: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EnergySourceVM", "Error updating energy source", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error updating energy source: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteEnergySource(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.deleteEnergySource(id)
                if (response.isSuccessful) {
                    loadEnergySources()
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to delete energy source: ${response.message()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EnergySourceVM", "Error deleting energy source", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error deleting energy source: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 