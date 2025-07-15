package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName

data class ChecklistChecklistItemCategoryDto(
    @SerializedName("\$type")
    val type: String = "ChecklistChecklistItemCategoryDataObject",
    @SerializedName("ChecklistId")
    val checklistId: String,
    @SerializedName("ChecklistItemCategoryId")
    val checklistItemCategoryId: String,
    @SerializedName("Id")
    val id: String,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("InternalObjectId")
    val internalObjectId: Int
) 