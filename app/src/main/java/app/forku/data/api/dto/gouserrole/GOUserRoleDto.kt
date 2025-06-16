package app.forku.data.api.dto.gouserrole

import com.google.gson.annotations.SerializedName

data class GOUserRoleDto(
    @SerializedName("GOUserId")
    val GOUserId: String,
    
    @SerializedName("GORoleName")
    val gORoleName: String,
    
    @SerializedName("IsActive")
    val isActive: Boolean = true,
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("IsDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int? = null
) 