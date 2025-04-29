package app.forku.data.api.dto.business

import com.google.gson.annotations.SerializedName

data class TimezoneDto(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Name")
    val name: String,
    
    @SerializedName("Offset")
    val offset: String,
    
    @SerializedName("businessConfiguration")
    val businessConfiguration: String? = null,
    
    @SerializedName("site")
    val site: String? = null,
    
    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("isDirty")
    val isDirty: Boolean = false,
    
    @SerializedName("isNew")
    val isNew: Boolean = false,
    
    @SerializedName("internalObjectId")
    val internalObjectId: Int = 0
) 