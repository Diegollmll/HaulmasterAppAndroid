package app.forku.domain.usecase.checklist

import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto
import app.forku.domain.repository.checklist.ChecklistItemAnswerMultimediaRepository
import javax.inject.Inject

class AddChecklistItemAnswerMultimediaUseCase @Inject constructor(
    private val repository: ChecklistItemAnswerMultimediaRepository
) {
    suspend operator fun invoke(entityJson: String): Result<ChecklistItemAnswerMultimediaDto> {
        return repository.addChecklistItemAnswerMultimedia(entityJson)
    }
} 