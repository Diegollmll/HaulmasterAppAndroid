package app.forku.domain.model.checklist

data class QuestionCategory(
    val id: String,
    val name: String,
    val description: String?,
    val order: Int,
    val questions: List<Question>
) 