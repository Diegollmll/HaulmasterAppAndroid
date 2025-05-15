package app.forku.data.api.dto.checklist

data class ChecklistDto(
    val `$type`: String = "ChecklistDataObject",
    val CriticalityLevels: List<Int>,
    val CriticalQuestionMinimum: Int,
    val Description: String,
    val EnergySources: Int,
    val Id: String,
    val IsDefault: Boolean,
    val MaxQuestionsPerCheck: Int,
    val RotationGroups: Int,
    val StandardQuestionMaximum: Int,
    val Title: String,
    val ChecklistChecklistQuestionItems: List<ChecklistItemDto>? = emptyList(),
    val IsMarkedForDeletion: Boolean,
    val InternalObjectId: Int
) 