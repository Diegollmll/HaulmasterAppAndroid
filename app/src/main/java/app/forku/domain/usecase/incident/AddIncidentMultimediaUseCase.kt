package app.forku.domain.usecase.incident

import app.forku.data.api.dto.incident.IncidentMultimediaDto
import app.forku.domain.repository.incident.IncidentMultimediaRepository
import javax.inject.Inject

class AddIncidentMultimediaUseCase @Inject constructor(
    private val repository: IncidentMultimediaRepository
) {
    suspend operator fun invoke(entityJson: String): Result<app.forku.data.api.dto.incident.IncidentMultimediaDto> {
        return repository.addIncidentMultimedia(entityJson)
    }
} 