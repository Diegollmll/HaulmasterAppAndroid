package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class AuthenticationRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
) 