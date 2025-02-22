package app.forku.domain.model

data class Checklist(
    val items: List<ChecklistItem>,
    val metadata: ChecklistMetadata,
    val rotationRules: RotationRules
)

data class ChecklistItem(
    val id: String,
    val category: String,
    val subCategory: String,
    val energySource: List<EnergySource>,
    val vehicleType: List<VehicleTypeEnum>,
    val component: String,
    val question: String,
    val description: String,
    val criticality: Criticality,
    val expectedAnswer: Answer,
    val rotationGroup: Int,
    val userAnswer: Answer? = null
)

data class ChecklistMetadata(
    val version: String,
    val lastUpdated: String,
    val totalQuestions: Int,
    val rotationGroups: Int,
    val questionsPerCheck: Int,
    val criticalityLevels: List<Criticality>,
    val energySources: List<EnergySource>
)

data class RotationRules(
    val maxQuestionsPerCheck: Int,
    val requiredCategories: List<String>,
    val criticalQuestionMinimum: Int,
    val standardQuestionMaximum: Int
)

enum class EnergySource {
    ALL, ELECTRIC, LPG, DIESEL
}

enum class VehicleTypeEnum {
    ALL
}

enum class Criticality {
    CRITICAL, STANDARD
}

enum class Answer {
    PASS, FAIL
}