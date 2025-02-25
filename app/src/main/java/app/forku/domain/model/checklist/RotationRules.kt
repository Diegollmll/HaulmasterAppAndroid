package app.forku.domain.model.checklist

data class RotationRules(
    val maxQuestionsPerCheck: Int,
    val requiredCategories: List<String>,
    val criticalQuestionMinimum: Int,
    val standardQuestionMaximum: Int
) {
    init {
        require(criticalQuestionMinimum + standardQuestionMaximum <= maxQuestionsPerCheck) {
            "Sum of minimum critical (${criticalQuestionMinimum}) and maximum standard (${standardQuestionMaximum}) " +
            "questions cannot exceed maxQuestionsPerCheck (${maxQuestionsPerCheck})"
        }
        
        require(requiredCategories.size <= maxQuestionsPerCheck) {
            "Number of required categories (${requiredCategories.size}) " +
            "cannot exceed maxQuestionsPerCheck (${maxQuestionsPerCheck})"
        }
    }
} 