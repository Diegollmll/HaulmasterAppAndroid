package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.Answer
import javax.inject.Inject

class ValidateChecklistCompletionUseCase @Inject constructor() {
    operator fun invoke(items: List<ChecklistItem>): ChecklistValidationResult {
        val allAnswered = items.all { it.userAnswer != null }
        val allPassed = items.all { it.userAnswer == Answer.PASS }
        val hasBlockingFailure = items.any { it.isCritical && it.userAnswer == Answer.FAIL }

        return ChecklistValidationResult(
            isComplete = allAnswered,
            isPassed = allPassed,
            isBlocked = hasBlockingFailure
        )
    }
}

data class ChecklistValidationResult(
    val isComplete: Boolean,
    val isPassed: Boolean,
    val isBlocked: Boolean
) 