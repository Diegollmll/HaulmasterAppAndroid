package app.forku.domain.usecase.vehiclefail_incident

import app.forku.data.dto.VehicleFailIncidentDto
import app.forku.domain.repository.incident.VehicleFailIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveVehicleFailIncidentUseCase @Inject constructor(
    private val repository: VehicleFailIncidentRepository
) {
    suspend operator fun invoke(
        entity: String,
        include: String? = null,
        dateformat: String? = "ISO8601"
    ): Flow<Result<VehicleFailIncidentDto>> {
        return repository.save(entity, include, dateformat)
    }
} 