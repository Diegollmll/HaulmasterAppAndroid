package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName

data class AnsweredChecklistItemDto(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("ChecklistAnswerId")
    val checklistAnswerId: String = "",
    @SerializedName("ChecklistVersion")
    val checklistVersion: String = "1.0",
    @SerializedName("ChecklistItemId")
    val checklistItemId: String = "",
    @SerializedName("ChecklistItemVersion")
    val checklistItemVersion: String = "1.0",
    @SerializedName("GOUserId")
    val goUserId: String = "",
    @SerializedName("UserAnswer")
    val userAnswer: Int = 0,
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("UserComment")
    val userComment: String? = null,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null
) 