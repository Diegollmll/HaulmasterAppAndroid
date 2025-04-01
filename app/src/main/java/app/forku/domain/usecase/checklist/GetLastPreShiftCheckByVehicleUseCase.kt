package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.checklist.ChecklistRepository
import javax.inject.Inject

class GetLastPreShiftCheckByVehicleUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(vehicleId: String): PreShiftCheck? {
        val allChecks = checklistRepository.getAllChecks()
        return allChecks
            .filter { check -> check.vehicleId == vehicleId }
            .maxByOrNull { check -> 
                check.lastCheckDateTime ?: "" // Provide empty string as fallback for null values
            }
    }
} 