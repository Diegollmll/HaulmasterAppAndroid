package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistItemCategoryDto
import app.forku.domain.model.checklist.ChecklistItemCategory

fun ChecklistItemCategoryDto.toDomain(): ChecklistItemCategory {
    return ChecklistItemCategory(
        id = id,
        name = name,
        description = description
    )
} 