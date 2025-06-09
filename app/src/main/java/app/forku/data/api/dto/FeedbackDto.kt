package app.forku.data.api.dto

import com.google.gson.annotations.SerializedName

data class FeedbackDto(
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("CanContactMe")
    val canContactMe: Boolean = false,
    @SerializedName("Comment")
    val comment: String,
    @SerializedName("GOUserId")
    val goUserId: String,
    @SerializedName("Rating")
    val rating: Int,
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false
) 