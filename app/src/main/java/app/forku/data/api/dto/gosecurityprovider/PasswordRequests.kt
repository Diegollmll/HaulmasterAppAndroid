package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class LostPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("newPassword")
    val newPassword: String
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,

    @SerializedName("newPassword")
    val newPassword: String,

    @SerializedName("userId")
    val userId: String? = null
) 