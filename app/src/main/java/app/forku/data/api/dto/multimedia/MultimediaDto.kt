package app.forku.data.api.dto.multimedia

import com.google.gson.annotations.SerializedName

data class MultimediaDto(
    @SerializedName("EntityType")
    val entityType: Int,
    
    @SerializedName("GOUserId")
    val goUserId: String,
    
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Image")
    val image: String?,
    
    @SerializedName("ImageFileSize")
    val imageFileSize: Int,
    
    @SerializedName("ImageInternalName")
    val imageInternalName: String?,
    
    @SerializedName("MultimediaType")
    val multimediaType: Int,
    
    @SerializedName("_gOUser_NewObjectId")
    val goUserNewObjectId: Int,
    
    @SerializedName("goUser")
    val goUser: Map<String, Any>?,
    
    @SerializedName("isMarkedForDeletion")
    val isMarkedForDeletion: Boolean,
    
    @SerializedName("isDirty")
    val isDirty: Boolean,
    
    @SerializedName("isNew")
    val isNew: Boolean,
    
    @SerializedName("internalObjectId")
    val internalObjectId: Int
) 