package app.forku.data.remote.api

import com.google.gson.annotations.SerializedName

data class UpdateBusinessRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("superAdminId")
    val superAdminId: String? = null
) {
    override fun toString(): String {
        return "UpdateBusinessRequest(name='$name', status='$status', superAdminId=$superAdminId)"
    }
} 