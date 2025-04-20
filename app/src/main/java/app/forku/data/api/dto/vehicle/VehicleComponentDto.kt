package app.forku.data.api.dto.vehicle

import com.google.gson.annotations.SerializedName

data class VehicleComponentDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) 