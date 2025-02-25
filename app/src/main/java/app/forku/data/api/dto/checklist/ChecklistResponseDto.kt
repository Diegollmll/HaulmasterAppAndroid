package app.forku.data.api.dto.checklist

typealias ChecklistResponseDto = ArrayList<ChecklistResponseDtoElement>

data class ChecklistResponseDtoElement(
    val items: List<ChecklistItemDto>,
    val metadata: ChecklistMetadataDto,
    val rotationRules: RotationRulesDto,
    val id: String
)
