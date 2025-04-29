package app.forku.data.api.dto.business

import com.google.gson.annotations.SerializedName

data class UserBusinessAssignmentDto(
    @SerializedName("BusinessId")
    val businessId: String,

    @SerializedName("GOUserId")
    val userId: String,

    @SerializedName("Role")
    val role: String? = null,

    @SerializedName("CreatedAt")
    val createdAt: String? = null,

    @SerializedName("UpdatedAt")
    val updatedAt: String? = null,

    @SerializedName("CreatedBy")
    val createdBy: String? = null,

    @SerializedName("UpdatedBy")
    val updatedBy: String? = null
) 