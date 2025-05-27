package app.forku.domain.usecase.collision_incident

import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollisionIncidentListUseCase @Inject constructor(
    private val repository: ICollisionIncidentRepository
) {
    suspend operator fun invoke(): Flow<Result<List<CollisionIncidentDto>>> {
        return repository.getCollisionIncidentList()
    }
} 