package app.forku.data.api.dto.user

import app.forku.data.api.dto.business.BusinessDto
import app.forku.data.api.dto.site.SiteDto
import com.google.gson.annotations.SerializedName

data class UserPreferencesDto(
    @SerializedName("\$type")
    val type: String = "UserPreferencesDataObject",
    
    @SerializedName("Id")
    val id: String? = null,
    
    @SerializedName("BusinessId")
    val businessId: String? = null,
    
    @SerializedName("SiteId")
    val siteId: String? = null,
    
    @SerializedName("LastSelectedBusinessId")
    val lastSelectedBusinessId: String? = null,
    
    @SerializedName("LastSelectedSiteId")
    val lastSelectedSiteId: String? = null,
    
    // Campos adicionales que podemos agregar en el futuro
    @SerializedName("Theme")
    val theme: String? = "system",
    
    @SerializedName("Language")
    val language: String? = "en",
    
    @SerializedName("NotificationsEnabled")
    val notificationsEnabled: Boolean = true,
    
    // ✅ GO API Required Fields
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int = 0,

    @SerializedName("Site")
    val site: SiteDto? = null,

    @SerializedName("GOUser")
    val user: app.forku.data.api.dto.user.UserDto? = null,

    @SerializedName("Business")
    val business: BusinessDto? = null,

    @SerializedName("GOUserId")
    val goUserId: String? = null

) 