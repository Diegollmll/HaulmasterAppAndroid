package app.forku.presentation.checklist

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.RotationRules
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.checklist.Answer

data class ChecklistState(
    val vehicle: Vehicle? = null,
    val vehicleId: String = "",
    val vehicleStatus: VehicleStatus = VehicleStatus.AVAILABLE,
    val checkItems: List<ChecklistItem> = emptyList(),
    val checklistId: String? = null,
    val checklistAnswerId: String? = null,
    val checkStatus: String = CheckStatus.NOT_STARTED.toString(),
    val isCompleted: Boolean = false,
    val isSubmitted: Boolean = false,
    val isReadOnly: Boolean = false,
    val startDateTime: String? = null,
    val elapsedTime: Long = 0L,
    val error: String? = null,
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val syncErrors: Map<String, String> = emptyMap(),
    val lastSyncedItemId: String? = null,
    val showErrorModal: Boolean = false,
    val errorModalMessage: String? = null,
    val isSubmitting: Boolean = false,
    val vehicleBlocked: Boolean = false,
    val message: String? = null,
    val noCompatibleChecklists: Boolean = false,
    val totalChecklistsFound: Int = 0,
    val compatibleChecklistsFound: Int = 0,
    val specificChecklistsFound: Int = 0,
    val defaultChecklistsFound: Int = 0
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

    val canSubmitFinal: Boolean
        get() = !isReadOnly && 
                !isSubmitting && 
                allAnswered && 
                !hasUnsavedChanges &&
                checkStatus == CheckStatus.IN_PROGRESS.toString()

    val showSubmitButton: Boolean
        get() = !isSubmitted && !isReadOnly

    val needsSync: Boolean
        get() = hasUnsavedChanges || syncErrors.isNotEmpty()
        
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

