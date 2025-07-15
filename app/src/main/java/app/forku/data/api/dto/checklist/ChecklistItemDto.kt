package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName

data class ChecklistItemDto(
    val `$type`: String = "ChecklistItemDataObject",
    val ChecklistId: String,
    @SerializedName("Version")
    val version: String = "1.0",
    @SerializedName("CreatedAt")
    val createdAt: String? = null,
    @SerializedName("ModifiedAt")
    val modifiedAt: String? = null,
    val ChecklistItemCategoryId: String,
    val ChecklistItemSubcategoryId: String,
    val Description: String,
    val EnergySource: List<Int>,
    val ExpectedAnswer: Int,
    val Id: String,
    val IsCritical: Boolean,
    val Question: String,
    val RotationGroup: Int,
    val VehicleComponent: Int,
    val IsMarkedForDeletion: Boolean,
    val InternalObjectId: Int,
    @SerializedName("GOUserId")
    val goUserId: String? = null,
    val userAnswer: Int? = null,
    @SerializedName("AllVehicleTypesEnabled")
    val AllVehicleTypesEnabled: Boolean? = null
)