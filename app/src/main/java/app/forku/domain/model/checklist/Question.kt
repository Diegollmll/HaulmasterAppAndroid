package app.forku.domain.model.checklist

data class Question(
    val id: String,
    val categoryId: String,
    val text: String,
    val expectedAnswer: Answer,
    val userAnswer: Answer? = null,
    val isRequired: Boolean = true,
    val order: Int
) 