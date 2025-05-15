package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter

// Adjust fields as needed based on the JSON structure
// Add more fields if required for your use case

data class ChecklistAnswerDto(
    @SerializedName("ChecklistId")
    val checklistId: String,
    @SerializedName("EndDateTime")
    val endDateTime: String?,
    @SerializedName("GOUserId")
    val goUserId: String,
    @SerializedName("Id")
    val id: String? = "",
    @SerializedName("StartDateTime")
    val startDateTime: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("LocationCoordinates")
    val locationCoordinates: String? = null,
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("LastCheckDateTime")
    val lastCheckDateTime: String,
    @SerializedName("VehicleId")
    val vehicleId: String,
    //@SerializedName("\$type")
    //val type: String = "ChecklistAnswerDataObject"
) 