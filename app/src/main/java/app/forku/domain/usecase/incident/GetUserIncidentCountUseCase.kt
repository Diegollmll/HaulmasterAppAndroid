package app.forku.domain.usecase.incident

import app.forku.domain.repository.incident.IncidentRepository
import javax.inject.Inject

class GetUserIncidentCountUseCase @Inject constructor(
    private val repository: IncidentRepository
) {
    suspend operator fun invoke(userId: String? = null): Int {
        return repository.getIncidentCountForUser(userId)
    }
} 