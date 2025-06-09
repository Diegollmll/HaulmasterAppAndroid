package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName
import app.forku.data.api.dto.multimedia.MultimediaDto

data class ChecklistItemAnswerMultimediaDto(
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("AnsweredChecklistItemId")
    val checklistItemAnswerId: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("createdAt_WithTimezoneOffset")
    val createdAtWithTimezoneOffset: String? = null,
    @SerializedName("CreationDateTime")
    val creationDateTime: String? = null,
    // --- Multimedia fields (opcional) ---
    @SerializedName("EntityType")
    val entityType: Int? = null,
    @SerializedName("GOUserId")
    val goUserId: String? = null,
    @SerializedName("Image")
    val image: String? = null,
    @SerializedName("ImageFileSize")
    val imageFileSize: Int? = null,
    @SerializedName("ImageInternalName")
    val imageInternalName: String? = null,
    @SerializedName("ImageUrl")
    val imageUrl: String? = null,
    @SerializedName("MultimediaType")
    val multimediaType: Int? = null,
    @SerializedName("GoUser")
    val goUser: Map<String, Any>? = null,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean? = null,
    @SerializedName("IsDirty")
    val isDirty: Boolean? = null,
    @SerializedName("IsNew")
    val isNew: Boolean? = null
) 