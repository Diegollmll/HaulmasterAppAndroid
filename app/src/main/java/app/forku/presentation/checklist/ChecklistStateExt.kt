package app.forku.presentation.checklist

import app.forku.domain.model.checklist.CheckStatus


fun ChecklistState.canSubmit(): Boolean {
    return isNotEmpty && allAnswered && !isSubmitting
}

fun ChecklistState.canComplete(): Boolean {
    return canSubmit() && !hasCriticalFail
}

fun ChecklistState.getCompletionMessage(): String? {
    return when (checkStatus) {
        CheckStatus.COMPLETED_PASS.toString() -> "Check completado exitosamente"
        CheckStatus.COMPLETED_FAIL.toString() -> "Check completado con fallas"
        else -> null
    }
} 