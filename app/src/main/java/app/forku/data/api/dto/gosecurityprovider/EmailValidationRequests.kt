package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class ValidateEmailChangeRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("userId")
    val userId: String? = null
)

data class ResendEmailValidationRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("email")
    val email: String? = null
) 