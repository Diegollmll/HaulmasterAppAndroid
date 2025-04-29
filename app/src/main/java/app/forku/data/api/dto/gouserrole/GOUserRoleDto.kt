package app.forku.data.api.dto.gouserrole

import com.google.gson.annotations.SerializedName

data class GOUserRoleDto(
    @SerializedName("GOUserId")
    val GOUserId: String,
    
    @SerializedName("Role")
    val role: GORoleDto,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("CreatedAt")
    val createdAt: String? = null,
    
    @SerializedName("UpdatedAt")
    val updatedAt: String? = null
)

data class GORoleDto(
    @SerializedName("Name")
    val Name: String
) 