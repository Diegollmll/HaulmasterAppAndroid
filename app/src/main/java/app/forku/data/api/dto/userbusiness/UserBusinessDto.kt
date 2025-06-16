package app.forku.data.api.dto.userbusiness

import com.google.gson.annotations.SerializedName

data class UserBusinessDto(
    @SerializedName("BusinessId")
    val businessId: String,
    
    @SerializedName("SiteId")
    val siteId: String? = null,
    
    @SerializedName("GOUserId")
    val goUserId: String,
    
    @SerializedName("IsDirty")
    val isDirty: Boolean? = null
) 