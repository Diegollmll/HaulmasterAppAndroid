package app.forku.domain.model.checklist

data class Checklist(
    val id: String,
    val title: String,
    val description: String,
    val version: String = "1.0",
    val businessId: String? = null,
    val goUserId: String? = null,
    val items: List<ChecklistItem>,
    val criticalityLevels: List<Int>,
    val criticalQuestionMinimum: Int,
    val energySources: List<Int>,
    val isDefault: Boolean,
    val maxQuestionsPerCheck: Int,
    val rotationGroups: Int,
    val standardQuestionMaximum: Int,
    val isMarkedForDeletion: Boolean,
    val internalObjectId: Int,
    val allVehicleTypesEnabled: Boolean = false,
    val supportedVehicleTypeIds: Set<String> = emptySet(),
    val requiredCategoryIds: Set<String> = emptySet(),
    val createdAt: String? = null,
    val modifiedAt: String? = null,
    val isActive: Boolean = true
)