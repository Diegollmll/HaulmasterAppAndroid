package app.forku.presentation.user.cico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.presentation.common.utils.getRelativeTimeSpanString

@HiltViewModel
class CicoHistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CicoHistoryState())
    val state = _state.asStateFlow()

    init {
        loadCicoHistory()
    }

    fun loadCicoHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val history = sessionRepository.getOperatorSessionHistory()
                val sessionsWithVehicles = history.map { session ->
                    try {
                        val vehicle = vehicleRepository.getVehicle(session.vehicleId)
                        CicoEntry(
                            id = session.id,
                            vehicleName = vehicle.codename,
                            date = getRelativeTimeSpanString(session.startTime),
                            checkInTime = getRelativeTimeSpanString(session.startTime),
                            checkOutTime = session.endTime?.let { getRelativeTimeSpanString(it) }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("CICO", "Error loading vehicle for session ${session.id}", e)
                        null
                    }
                }.filterNotNull()

                _state.update { 
                    it.copy(
                        isLoading = false,
                        cicoHistory = sessionsWithVehicles,
                        error = if (sessionsWithVehicles.isEmpty()) "No history found" else null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("CICO", "Error loading CICO history", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load CICO history: ${e.message}"
                    )
                }
            }
        }
    }
}

data class CicoHistoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cicoHistory: List<CicoEntry> = emptyList()
)

data class CicoEntry(
    val id: String,
    val vehicleName: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?
) 