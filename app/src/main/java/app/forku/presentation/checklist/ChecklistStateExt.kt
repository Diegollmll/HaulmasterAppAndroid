package app.forku.presentation.checklist

import app.forku.domain.model.checklist.CheckStatus

fun ChecklistState.canSubmit(): Boolean {
    return isNotEmpty && 
           allAnswered && 
           !isSubmitting && 
           !isReadOnly &&
           checkStatus == CheckStatus.IN_PROGRESS.toString()
}

fun ChecklistState.canComplete(): Boolean {
    return canSubmit() && !hasCriticalFail && !hasUnsavedChanges
}

fun ChecklistState.needsSyncBeforeSubmit(): Boolean {
    return hasUnsavedChanges || syncErrors.isNotEmpty()
}

fun ChecklistState.isReadyForSubmission(): Boolean {
    return canSubmit() && !needsSyncBeforeSubmit()
}

fun ChecklistState.getCompletionMessage(): String? {
    return when {
        hasUnsavedChanges -> "Hay cambios sin guardar"
        syncErrors.isNotEmpty() -> "Hay errores de sincronización pendientes"
        checkStatus == CheckStatus.COMPLETED_PASS.toString() -> "Check completado exitosamente"
        checkStatus == CheckStatus.COMPLETED_FAIL.toString() -> "Check completado con fallas"
        else -> null
    }
}

fun ChecklistState.getSyncErrorMessage(): String? {
    return if (syncErrors.isNotEmpty()) {
        "Hay ${syncErrors.size} respuestas con errores de sincronización"
    } else null
} 