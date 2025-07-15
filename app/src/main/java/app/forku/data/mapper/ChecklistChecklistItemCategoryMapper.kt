package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistChecklistItemCategoryDto
import app.forku.domain.model.checklist.ChecklistChecklistItemCategory

fun ChecklistChecklistItemCategoryDto.toDomain(): ChecklistChecklistItemCategory {
    return ChecklistChecklistItemCategory(
        id = id,
        checklistId = checklistId,
        checklistItemCategoryId = checklistItemCategoryId,
        isMarkedForDeletion = isMarkedForDeletion,
        internalObjectId = internalObjectId
    )
}

fun ChecklistChecklistItemCategory.toDto(): ChecklistChecklistItemCategoryDto {
    return ChecklistChecklistItemCategoryDto(
        id = id,
        checklistId = checklistId,
        checklistItemCategoryId = checklistItemCategoryId,
        isMarkedForDeletion = isMarkedForDeletion,
        internalObjectId = internalObjectId
    )
} 