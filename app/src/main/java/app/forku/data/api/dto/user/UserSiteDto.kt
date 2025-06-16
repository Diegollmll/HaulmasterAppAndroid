package app.forku.data.api.dto.user

import com.google.gson.annotations.SerializedName

data class UserSiteDto(
    @SerializedName("Id")
    val id: String? = null,
    
    @SerializedName("SiteId")
    val siteId: String,
    
    @SerializedName("GOUserId")
    val goUserId: String,
    
    @SerializedName("IsDirty")
    val isDirty: Boolean? = null,
    
    @SerializedName("\$type")
    val type: String? = null
) 