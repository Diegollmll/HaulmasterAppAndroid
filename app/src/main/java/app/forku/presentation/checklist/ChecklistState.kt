package app.forku.presentation.checklist

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.RotationRules
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.checklist.Answer

data class ChecklistState(
    val vehicle: Vehicle? = null,
    val checkItems: List<ChecklistItem> = emptyList(),
    val rotationRules: RotationRules? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val isSubmitted: Boolean = false,
    val vehicleBlocked: Boolean = false,
    val error: String? = null,
    val checkId: String? = null,
    val vehicleId: String = "",
    val showErrorModal: Boolean = false,
    val errorModalMessage: String? = null,
    val vehicleStatus: VehicleStatus,
    val message: String? = null,
    val lastSavedAt: String? = null,
    val checkStatus: String,
    val isReadOnly: Boolean = false,
    val startDateTime: String? = null,
    val elapsedTime: Long = 0L
) {
    val isEmpty: Boolean
        get() = checkItems.isEmpty()

    val isNotEmpty: Boolean
        get() = checkItems.isNotEmpty()

    val allAnswered: Boolean
        get() = checkItems.all { it.userAnswer != null }

    val hasCriticalFail: Boolean
        get() = checkItems.any { item -> 
            item.isCritical && item.userAnswer == Answer.FAIL 
        }

    val showSubmitButton: Boolean
        get() = !isReadOnly && checkStatus == CheckStatus.IN_PROGRESS.toString()
        
    val formattedElapsedTime: String
        get() {
            try {
                val totalMinutes = elapsedTime / (1000 * 60)
                if (totalMinutes < 60) {
                    return String.format("%02d:%02d", totalMinutes, (elapsedTime / 1000) % 60)
                } else {
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60
                    val seconds = (elapsedTime / 1000) % 60
                    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
            } catch (e: Exception) {
                return "00:00:00"
            }
        }
}

