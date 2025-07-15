package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName

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
    @SerializedName("Version")
    val version: String = "1.0",
    @SerializedName("CreatedAt")
    val createdAt: String? = null,
    @SerializedName("ModifiedAt")
    val modifiedAt: String? = null,
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    val AllVehicleTypesEnabled: Boolean = false,
    val ChecklistChecklistItemCategoryItems: List<ChecklistChecklistItemCategoryDto> = emptyList(),
    val ChecklistVehicleTypeItems: List<ChecklistVehicleTypeDto> = emptyList(),
    val ChecklistChecklistQuestionItems: List<ChecklistItemDto>? = emptyList(),
    val IsMarkedForDeletion: Boolean,
    val InternalObjectId: Int,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("GOUserId")
    val goUserId: String? = null,
    val IsDirty: Boolean = true,
    val IsNew: Boolean = true,
    val criticalityLevelsEnumValues: List<Int> = emptyList(),
    val energySourceEnumValues: List<Int> = emptyList(),
    val CriticalityLevelsValues: List<SelectValue> = emptyList(),
    val EnergySourcesValues: List<SelectValue> = emptyList()
)

data class SelectValue(
    val selectvalue: Int
) 