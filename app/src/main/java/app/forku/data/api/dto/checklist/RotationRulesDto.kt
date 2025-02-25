package app.forku.data.api.dto.checklist

data class RotationRulesDto(
    val maxQuestionsPerCheck: Int,
    val requiredCategories: List<String>,
    val criticalQuestionMinimum: Int,
    val standardQuestionMaximum: Int
) 