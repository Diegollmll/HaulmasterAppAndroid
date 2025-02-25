package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject

class SubmitChecklistUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(
        vehicleId: String,
        items: List<ChecklistItem>,
        checkId: String? = null
    ): PreShiftCheck {
        return repository.submitPreShiftCheck(vehicleId, items, checkId)
    }
} 