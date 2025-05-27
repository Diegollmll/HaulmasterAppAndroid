package app.forku.domain.usecase.collision_incident

import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveCollisionIncidentUseCase @Inject constructor(
    private val repository: ICollisionIncidentRepository
) {
    suspend operator fun invoke(
        incident: CollisionIncidentDto,
        include: String? = null,
        dateformat: String? = "ISO8601"
    ): Flow<Result<CollisionIncidentDto>> {
        return repository.saveCollisionIncident(incident, include, dateformat)
    }
} 