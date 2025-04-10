package app.forku.domain.repository

import app.forku.data.model.QuestionaryChecklistDto

interface QuestionaryChecklistRepository {
    suspend fun getAllQuestionaries(): List<QuestionaryChecklistDto>
    suspend fun getQuestionaryById(id: String): QuestionaryChecklistDto
    suspend fun createQuestionary(questionary: QuestionaryChecklistDto): QuestionaryChecklistDto
    suspend fun updateQuestionary(id: String, questionary: QuestionaryChecklistDto): QuestionaryChecklistDto
    suspend fun deleteQuestionary(id: String)
} 