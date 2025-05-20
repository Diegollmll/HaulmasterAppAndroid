package app.forku.data.api.dto.gosecurityprovider

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("Id")
    val id: String? = null,
    
    @SerializedName("Email")
    val email: String,

    @SerializedName("Password")
    val password: String,

    @SerializedName("FirstName")
    val firstName: String,

    @SerializedName("Surname")
    val lastName: String,

) 