package app.forku.data.api.dto.gogroup

import com.google.gson.annotations.SerializedName

data class GOGroupRoleDto(
    @SerializedName("GOGroupName")
    val gOGroupName: String,
    
    @SerializedName("GORoleName")
    val gORoleName: String,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("CreatedAt")
    val createdAt: String?,
    
    @SerializedName("UpdatedAt")
    val updatedAt: String?
) 