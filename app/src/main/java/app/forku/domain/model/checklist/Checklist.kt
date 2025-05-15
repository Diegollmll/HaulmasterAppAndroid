package app.forku.domain.model.checklist

data class Checklist(
    val id: String,
    val title: String,
    val description: String,
    val items: List<ChecklistItem>,
    val criticalityLevels: List<Int>,
    val criticalQuestionMinimum: Int,
    val energySources: Int,
    val isDefault: Boolean,
    val maxQuestionsPerCheck: Int,
    val rotationGroups: Int,
    val standardQuestionMaximum: Int,
    val isMarkedForDeletion: Boolean,
    val internalObjectId: Int
)