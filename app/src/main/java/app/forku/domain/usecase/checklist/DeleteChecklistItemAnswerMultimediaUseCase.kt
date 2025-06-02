package app.forku.domain.usecase.checklist

import app.forku.domain.repository.checklist.ChecklistItemAnswerMultimediaRepository
import javax.inject.Inject

class DeleteChecklistItemAnswerMultimediaUseCase @Inject constructor(
    private val repository: ChecklistItemAnswerMultimediaRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteChecklistItemAnswerMultimedia(id)
    }
} 