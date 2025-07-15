package app.forku.data.api.dto.checklist

import com.google.gson.annotations.SerializedName

data class ChecklistItemSubcategoryDto(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("ChecklistItemCategoryId1")
    val categoryId: String = "",
    @SerializedName("Name")
    val name: String = ""
) 