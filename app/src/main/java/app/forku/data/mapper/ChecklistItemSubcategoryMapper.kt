package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistItemSubcategoryDto
import app.forku.domain.model.checklist.ChecklistItemSubcategory

fun ChecklistItemSubcategoryDto.toDomain(): ChecklistItemSubcategory {
    return ChecklistItemSubcategory(
        id = id,
        categoryId = categoryId,
        name = name
    )
}

fun ChecklistItemSubcategory.toDto(): ChecklistItemSubcategoryDto {
    return ChecklistItemSubcategoryDto(
        id = id,
        categoryId = categoryId,
        name = name
    )
} 