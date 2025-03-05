package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.Checklist
import app.forku.domain.repository.checklist.ChecklistRepository
import javax.inject.Inject

class GetChecklistUseCase @Inject constructor(
    private val repository: ChecklistRepository
) {
    suspend operator fun invoke(vehicleId: String): List<Checklist> {
        return repository.getChecklistItems(vehicleId)
    }
} 