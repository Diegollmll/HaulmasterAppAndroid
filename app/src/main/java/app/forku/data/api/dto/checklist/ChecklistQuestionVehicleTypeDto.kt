package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName

data class ChecklistQuestionVehicleTypeDto(
    @SerializedName("\$type")
    val type: String = "ChecklistQuestionVehicleTypeDataObject",
    @SerializedName("ChecklistItemId")
    val checklistItemId: String,
    @SerializedName("VehicleTypeId")
    val vehicleTypeId: String,
    @SerializedName("Id")
    val id: String,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("InternalObjectId")
    val internalObjectId: Int
) 