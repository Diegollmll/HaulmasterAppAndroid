package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.checklist.PreShiftStatus
import javax.inject.Inject

class ValidateChecklistUseCase @Inject constructor() {
    operator fun invoke(items: List<ChecklistItem>): ChecklistValidation {
        val allItemsAnswered = items.all { it.userAnswer != null }
        val hasCriticalFail = items.any { item -> 
            item.isCritical && item.userAnswer == Answer.FAIL 
        }

        val status = when {
            !allItemsAnswered -> PreShiftStatus.IN_PROGRESS
            hasCriticalFail -> PreShiftStatus.COMPLETED_FAIL
            else -> PreShiftStatus.COMPLETED_PASS
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
    val status: PreShiftStatus,
    val isComplete: Boolean,
    val isBlocked: Boolean,
    val canStartSession: Boolean
) 