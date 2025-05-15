package app.forku.data.api.dto.vehicle

import com.google.gson.annotations.SerializedName

data class VehicleTypeDto(
    @SerializedName("Id")
    val Id: String,

    @SerializedName("Name")
    val Name: String,

    @SerializedName("RequiresCertification")
    val RequiresCertification: Boolean = false,

    @SerializedName("VehicleCategoryId")
    val VehicleCategoryId: String? = null,

    @SerializedName("IsMarkedForDeletion")
    val IsMarkedForDeletion: Boolean = false,

    @SerializedName("InternalObjectId")
    val InternalObjectId: Int = 0
) 