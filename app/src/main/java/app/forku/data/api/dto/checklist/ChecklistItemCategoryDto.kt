package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName
import app.forku.domain.model.checklist.ChecklistItemCategory

data class ChecklistItemCategoryDto(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("Name")
    val name: String = "",
    val description: String? = null
) 