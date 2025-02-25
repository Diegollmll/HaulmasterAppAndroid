package app.forku.domain.model.checklist

data class Checklist(
    val items: List<ChecklistItem>,
    val metadata: ChecklistMetadata,
    val rotationRules: RotationRules
)