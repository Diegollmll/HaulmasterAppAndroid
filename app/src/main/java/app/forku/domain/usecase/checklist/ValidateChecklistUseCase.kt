package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.CheckStatus
import javax.inject.Inject

class ValidateChecklistUseCase @Inject constructor() {
    operator fun invoke(items: List<ChecklistItem>): ChecklistValidation {
        val allItemsAnswered = items.all { it.userAnswer != null }
        val hasCriticalFail = items.any { item -> 
            item.isCritical && item.userAnswer == Answer.FAIL 
        }

        val status = when {
            hasCriticalFail -> CheckStatus.COMPLETED_FAIL
            allItemsAnswered && !hasCriticalFail -> CheckStatus.COMPLETED_PASS
            else -> CheckStatus.IN_PROGRESS
            //forceInProgress -> PreShiftStatus.IN_PROGRESS
        }

        return ChecklistValidation(
            status = status,
            isComplete = allItemsAnswered,
            isBlocked = hasCriticalFail,
            canStartSession = allItemsAnswered && !hasCriticalFail
        )
    }
}

data class ChecklistValidation(
    val status: CheckStatus,
    val isComplete: Boolean,
    val isBlocked: Boolean,
    val canStartSession: Boolean
) 