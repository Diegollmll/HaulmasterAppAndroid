package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.vehicle.VehicleDto

// Adjust fields as needed based on the JSON structure
// Add more fields if required for your use case

data class ChecklistAnswerDto(
    @SerializedName("ChecklistId")
    val checklistId: String,
    @SerializedName("ChecklistVersion")
    val checklistVersion: String = "1.0",
    @SerializedName("EndDateTime")
    val endDateTime: String?,
    @SerializedName("GOUserId")
    val goUserId: String,
    @SerializedName("Id")
    val id: String? = "",
    @SerializedName("StartDateTime")
    val startDateTime: String,
    @SerializedName("Status")
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
    @SerializedName("Duration")
    val duration: Int? = null,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null,
    @SerializedName("GOUser")
    @Contextual
    val goUser: UserDto? = null,
    @SerializedName("Vehicle")
    @Contextual
    val vehicle: VehicleDto? = null
    //@SerializedName("\$type")
    //val type: String = "ChecklistAnswerDataObject"
) 