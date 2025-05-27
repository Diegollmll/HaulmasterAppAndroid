package app.forku.domain.usecase.collision_incident

import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteCollisionIncidentUseCase @Inject constructor(
    private val repository: ICollisionIncidentRepository
) {
    suspend operator fun invoke(id: Long): Flow<Result<Unit>> {
        return repository.deleteCollisionIncidentById(id)
    }
} 