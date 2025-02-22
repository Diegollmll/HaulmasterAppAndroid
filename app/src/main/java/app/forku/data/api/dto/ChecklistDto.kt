package app.forku.data.api.dto

data class ChecklistResponseDto(
    val items: List<ChecklistItemDto>,
    val metadata: ChecklistMetadataDto,
    val rotationRules: RotationRulesDto
)

data class ChecklistItemDto(
    val id: String,
    val category: String,
    val subCategory: String,
    val energySource: List<String>,
    val vehicleType: List<String>,
    val component: String,
    val question: String,
    val description: String,
    val criticality: String,
    val expectedAnswer: String,
    val rotationGroup: Int
)

data class ChecklistMetadataDto(
    val version: String,
    val lastUpdated: String,
    val totalQuestions: Int,
    val rotationGroups: Int,
    val questionsPerCheck: Int,
    val criticalityLevels: List<String>,
    val energySources: List<String>
)

data class RotationRulesDto(
    val maxQuestionsPerCheck: Int,
    val requiredCategories: List<String>,
    val criticalQuestionMinimum: Int,
    val standardQuestionMaximum: Int
)