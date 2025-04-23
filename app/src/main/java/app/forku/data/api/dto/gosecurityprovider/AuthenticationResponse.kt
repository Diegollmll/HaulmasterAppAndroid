package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class AuthenticationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("token")
    val token: String? = null
) 