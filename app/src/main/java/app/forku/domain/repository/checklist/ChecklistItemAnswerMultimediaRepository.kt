package app.forku.domain.repository.checklist

import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto
 
interface ChecklistItemAnswerMultimediaRepository {
    suspend fun addChecklistItemAnswerMultimedia(entityJson: String): Result<ChecklistItemAnswerMultimediaDto>
    suspend fun getChecklistItemAnswerMultimediaByAnswerId(answerId: String): Result<List<ChecklistItemAnswerMultimediaDto>>
    suspend fun deleteChecklistItemAnswerMultimedia(id: String): Result<Unit>
} 