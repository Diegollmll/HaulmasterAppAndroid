package app.forku.data.api.dto.gogroup

import com.google.gson.annotations.SerializedName

data class GOGroupDto(
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Description")
    val description: String?,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("CreatedAt")
    val createdAt: String?,
    
    @SerializedName("UpdatedAt")
    val updatedAt: String?
) 