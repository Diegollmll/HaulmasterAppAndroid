package app.forku.domain.usecase.nearmiss_incident

import app.forku.data.dto.NearMissIncidentDto
import app.forku.data.repository.NearMissIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveNearMissIncidentUseCase @Inject constructor(
    private val repository: NearMissIncidentRepository
) {
    suspend operator fun invoke(
        incident: NearMissIncidentDto
    ): Flow<Result<NearMissIncidentDto>> {
        return repository.saveNearMissIncident(incident)
    }
} 