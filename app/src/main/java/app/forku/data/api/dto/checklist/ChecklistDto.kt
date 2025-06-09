package app.forku.data.api.dto.checklist

data class ChecklistDto(
    val `$type`: String = "ChecklistDataObject",
    val CriticalityLevels: List<Int>,
    val CriticalQuestionMinimum: Int,
    val Description: String,
    val EnergySources: List<Int>,
    val Id: String,
    val IsDefault: Boolean,
    val MaxQuestionsPerCheck: Int,
    val RotationGroups: Int,
    val StandardQuestionMaximum: Int,
    val Title: String,
    val AllVehicleTypesEnabled: Boolean = false,
    val ChecklistChecklistItemCategoryItems: List<ChecklistChecklistItemCategoryDto> = emptyList(),
    val ChecklistVehicleTypeItems: List<ChecklistVehicleTypeDto> = emptyList(),
    val ChecklistChecklistQuestionItems: List<ChecklistItemDto>? = emptyList(),
    val IsMarkedForDeletion: Boolean,
    val InternalObjectId: Int
) 