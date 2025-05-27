package app.forku.domain.usecase.collision_incident

import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCollisionIncidentCountUseCase @Inject constructor(
    private val repository: ICollisionIncidentRepository
) {
    suspend operator fun invoke(): Flow<Result<Int>> {
        return repository.getCollisionIncidentCount()
    }
} 