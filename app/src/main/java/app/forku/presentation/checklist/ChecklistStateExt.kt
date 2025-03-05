package app.forku.presentation.checklist

import app.forku.domain.model.checklist.PreShiftStatus
import app.forku.domain.model.checklist.Answer


fun ChecklistState.canSubmit(): Boolean {
    return isNotEmpty && allAnswered && !isSubmitting
}

fun ChecklistState.canComplete(): Boolean {
    return canSubmit() && !hasCriticalFail
}

fun ChecklistState.getCompletionMessage(): String? {
    return when (checkStatus) {
        PreShiftStatus.COMPLETED_PASS.toString() -> "Check completado exitosamente"
        PreShiftStatus.COMPLETED_FAIL.toString() -> "Check completado con fallas"
        else -> null
    }
} 