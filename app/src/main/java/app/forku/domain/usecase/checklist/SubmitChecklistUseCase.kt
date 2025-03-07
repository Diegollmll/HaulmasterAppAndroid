package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.repository.checklist.ChecklistRepository
import javax.inject.Inject

class SubmitChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(
        vehicleId: String,
        items: List<ChecklistItem>,
        checkId: String? = null,
        status: String = CheckStatus.IN_PROGRESS.toString()
    ): PreShiftCheck {
        return checklistRepository.submitPreShiftCheck(
            vehicleId = vehicleId,
            checkItems = items,
            checkId = checkId,
            status = status
        )
    }
} 