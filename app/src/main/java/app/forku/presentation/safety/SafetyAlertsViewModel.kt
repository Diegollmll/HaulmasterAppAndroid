package app.forku.presentation.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.checklist.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.domain.model.checklist.Answer
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository
import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime

@HiltViewModel
class SafetyAlertsViewModel @Inject constructor(
    private val checklistRepository: ChecklistRepository,
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SafetyAlertsState())
    val state: StateFlow<SafetyAlertsState> = _state.asStateFlow()

    init {
        loadSafetyAlerts()
    }

    fun loadSafetyAlerts() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                // Get all checks
                val checks = checklistRepository.getAllChecks()
                
                // Process checks to find safety alerts (failed non-critical items)
                val alerts = mutableListOf<SafetyAlert>()
                
                checks.forEach { check ->
                    // Get vehicle info
                    val vehicle = vehicleRepository.getVehicle(check.vehicleId)
                    // Get operator info
                    val operator = userRepository.getUserById(check.userId)
                    
                    // Find failed non-critical items
                    check.items
                        .filter { !it.isCritical && it.userAnswer == Answer.FAIL }
                        .forEach { item ->
                            val alert = SafetyAlert(
                                id = "${check.id}_${item.id}",
                                vehicleId = check.vehicleId,
                                vehicleCodename = vehicle.codename,
                                description = item.description,
                                operatorId = check.userId,
                                operatorName = "${operator?.firstName?.first() ?: ""}. ${operator?.lastName ?: "Unknown"}",
                                date = check.lastCheckDateTime?.let { parseDateTime(it) } ?: "Date not available"
                            )
                            alerts.add(alert)
                        }
                }

                _state.value = _state.value.copy(
                    safetyAlerts = alerts.sortedByDescending { it.date },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun parseDateTime(timestamp: String): String {
        return try {
            val dateTime = OffsetDateTime.parse(timestamp)
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            dateTime.format(formatter)
        } catch (e: Exception) {
            timestamp
        }
    }
} 