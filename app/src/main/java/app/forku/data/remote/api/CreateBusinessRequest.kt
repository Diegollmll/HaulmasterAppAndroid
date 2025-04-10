package app.forku.data.remote.api

import com.google.gson.annotations.SerializedName

data class CreateBusinessRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("status")
    val status: String = "PENDING",

    @SerializedName("systemOwnerId")
    val systemOwnerId: String?,

    @SerializedName("superAdminId")
    val superAdminId: String?
) {
    override fun toString(): String {
        return "CreateBusinessRequest(name='$name', status='$status', systemOwnerId='$systemOwnerId', superAdminId='$superAdminId')"
    }
} 