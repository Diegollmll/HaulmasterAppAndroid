package app.forku.presentation.vehicle.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.presentation.dashboard.VehicleSessionInfo

@HiltViewModel
class VehicleSessionListViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val sessionRepository: SessionRepository,
    private val checklistRepository: ChecklistRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleSessionListState())
    val state = _state.asStateFlow()

    init {
        loadVehicles()
    }

    private suspend fun getVehicleSessionInfo(vehicleId: String, userId: String): VehicleSessionInfo? {
        return try {
            android.util.Log.d("VehicleSessionList", "Getting info for vehicle: $vehicleId")
            val vehicle = vehicleRepository.getVehicle(vehicleId)
            android.util.Log.d("VehicleSessionList", "Found vehicle: $vehicle")
            val operator = userRepository.getUserById(userId)
            android.util.Log.d("VehicleSessionList", "Found operator: $operator")
            
            // Calculate session progress (assuming 8-hour shifts)
            val session = sessionRepository.getActiveSessionForVehicle(vehicleId) ?: return null
            val startTime = java.time.OffsetDateTime.parse(session.startTime)
            val now = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
            val elapsedMinutes = java.time.Duration.between(startTime, now).toMinutes()
            val progress = (elapsedMinutes.toFloat() / (8 * 60)).coerceIn(0f, 1f)
            
            VehicleSessionInfo(
                vehicleId = vehicle.id,
                vehicleType = vehicle.type.displayName,
                progress = progress,
                operatorName = operator?.let { "${it.firstName.first()}. ${it.lastName}" } ?: "Unknown",
                operatorImage = operator?.photoUrl,
                sessionStartTime = session.startTime,
                vehicleImage = vehicle.photoModel,
                codename = vehicle.codename
            )
        } catch (e: Exception) {
            android.util.Log.e("VehicleSessionList", "Error getting vehicle session info", e)
            null
        }
    }

    fun loadVehicles(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                )

                val vehicles = vehicleRepository.getVehicles()
                
                val vehiclesWithInfo = coroutineScope {
                    vehicles.map { vehicle ->
                        async {
                            val session = sessionRepository.getActiveSessionForVehicle(vehicle.id)
                            val lastCheck = checklistRepository.getLastPreShiftCheck(vehicle.id)
                            
                            VehicleWithSessionInfo(
                                vehicle = vehicle,
                                activeSession = session?.let { getVehicleSessionInfo(vehicle.id, it.userId) },
                                preShiftStatus = lastCheck?.status ?: CheckStatus.NOT_STARTED.toString()
                            )
                        }
                    }.map { it.await() }
                }

                _state.value = _state.value.copy(
                    vehicles = vehiclesWithInfo,
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error occurred",
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    fun refresh() {
        loadVehicles(showLoading = false)
    }

    fun refreshWithLoading() {
        loadVehicles(showLoading = true)
    }
} 