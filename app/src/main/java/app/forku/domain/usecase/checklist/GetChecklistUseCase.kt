package app.forku.domain.usecase.checklist

import app.forku.domain.model.checklist.Checklist
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject

class GetChecklistUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: String): List<Checklist> {
        return repository.getChecklistItems(vehicleId)
    }
} 