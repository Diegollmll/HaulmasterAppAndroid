package app.forku.data.api.dto.certification

import com.google.gson.annotations.SerializedName

data class CertificationVehicleTypeDto(
    @SerializedName("Id")
    val id: String,
    @SerializedName("CertificationId")
    val certificationId: String,
    @SerializedName("VehicleTypeId")
    val vehicleTypeId: String,
    @SerializedName("SiteId")
    val siteId: String? = null,
    @SerializedName("Timestamp")
    val timestamp: String? = null,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("IsDirty")
    val isDirty: Boolean = false,
    @SerializedName("IsNew")
    val isNew: Boolean = false,
    @SerializedName("InternalObjectId")
    val internalObjectId: Int = 0
) 