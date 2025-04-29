package app.forku.data.api.dto.checklist

import app.forku.domain.model.checklist.ChecklistItemCategory

data class ChecklistItemCategoryDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null
) 