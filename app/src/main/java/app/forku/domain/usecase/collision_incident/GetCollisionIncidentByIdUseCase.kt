package app.forku.domain.usecase.collision_incident

import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollisionIncidentByIdUseCase @Inject constructor(
    private val repository: ICollisionIncidentRepository
) {
    suspend operator fun invoke(id: Long): Flow<Result<CollisionIncidentDto>> {
        return repository.getCollisionIncidentById(id)
    }
} 