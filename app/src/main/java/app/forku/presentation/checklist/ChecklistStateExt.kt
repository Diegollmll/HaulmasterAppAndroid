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
        hasUnsavedChanges -> "There are unsaved changes"
        syncErrors.isNotEmpty() -> "There are pending synchronization errors"
        checkStatus == CheckStatus.COMPLETED_PASS.toString() -> "Check completed successfully"
        checkStatus == CheckStatus.COMPLETED_FAIL.toString() -> "Check completed with failures"
        else -> null
    }
}

fun ChecklistState.getSyncErrorMessage(): String? {
    return if (syncErrors.isNotEmpty()) {
        "There are ${syncErrors.size} responses with synchronization errors"
    } else null
} 