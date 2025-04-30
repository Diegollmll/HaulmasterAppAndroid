package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class BlockUserRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("reason")
    val reason: String? = null
)

data class ApproveUserRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("approvedBy")
    val approvedBy: String? = null
)

data class UnregisterRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("deleteData")
    val deleteData: Boolean = false
) 