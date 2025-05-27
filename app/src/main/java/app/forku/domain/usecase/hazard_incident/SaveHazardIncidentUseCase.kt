package app.forku.domain.usecase.hazard_incident

import app.forku.data.dto.HazardIncidentDto
import app.forku.data.repository.HazardIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveHazardIncidentUseCase @Inject constructor(
    private val repository: HazardIncidentRepository
) {
    suspend operator fun invoke(
        incident: HazardIncidentDto,
        include: String? = null,
        dateformat: String? = "ISO8601"
    ): Flow<Result<HazardIncidentDto>> {
        return repository.saveHazardIncident(incident, include, dateformat)
    }
} 