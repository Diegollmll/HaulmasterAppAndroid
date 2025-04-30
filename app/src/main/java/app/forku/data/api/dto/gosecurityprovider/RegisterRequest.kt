package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("businessId")
    val businessId: String? = null,

    @SerializedName("siteId")
    val siteId: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("isApproved")
    val isApproved: Boolean = false
) 