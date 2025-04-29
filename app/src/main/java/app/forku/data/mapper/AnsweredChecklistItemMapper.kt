package app.forku.data.mapper

import app.forku.data.api.dto.checklist.AnsweredChecklistItemDto
import app.forku.domain.model.checklist.AnsweredChecklistItem

fun AnsweredChecklistItemDto.toDomain(): AnsweredChecklistItem =
    AnsweredChecklistItem(
        id = id,
        checklistId = checklistId,
        question = question,
        answer = answer,
        userId = userId,
        createdAt = createdAt
    )

fun AnsweredChecklistItem.toDto(): AnsweredChecklistItemDto =
    AnsweredChecklistItemDto(
        id = id,
        checklistId = checklistId,
        question = question,
        answer = answer,
        userId = userId,
        createdAt = createdAt
    ) 