package app.forku.data.remote.api

import com.google.gson.annotations.SerializedName

data class CreateBusinessRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("status")
    val status: String = "PENDING",

    @SerializedName("system_owner_id")
    val systemOwnerId: String?,

    @SerializedName("super_admin_id")
    val superAdminId: String?
) {
    override fun toString(): String {
        return "CreateBusinessRequest(name='$name', status='$status', systemOwnerId='$systemOwnerId', superAdminId='$superAdminId')"
    }
} 