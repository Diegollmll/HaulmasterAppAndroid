package app.forku.data.api.dto.safetyalert

import com.google.gson.annotations.SerializedName

data class SafetyAlertDto(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("AnsweredChecklistItemId")
    val answeredChecklistItemId: String = "",
    @SerializedName("GOUserId")
    val goUserId: String = "",
    @SerializedName("VehicleId")
    val vehicleId: String = "",
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null,
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("CreationDateTime")
    val creationDateTime: String? = null
) 