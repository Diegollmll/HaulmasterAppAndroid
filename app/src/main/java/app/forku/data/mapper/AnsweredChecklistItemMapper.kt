package app.forku.data.mapper

import app.forku.data.api.dto.checklist.AnsweredChecklistItemDto
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.model.checklist.Answer

fun AnsweredChecklistItemDto.toDomain(): AnsweredChecklistItem =
    AnsweredChecklistItem(
        id = id,
        checklistId = "",
        checklistVersion = checklistVersion,
        checklistAnswerId = checklistAnswerId,
        checklistItemId = checklistItemId,
        checklistItemVersion = checklistItemVersion,
        question = "",
        answer = Answer.values().getOrNull(userAnswer)?.name ?: Answer.PASS.name,
        userId = goUserId,
        createdAt = "",
        userComment = userComment,
        businessId = businessId
    )

fun AnsweredChecklistItem.toDto(): AnsweredChecklistItemDto =
    AnsweredChecklistItemDto(
        id = id,
        checklistAnswerId = checklistAnswerId,
        checklistVersion = checklistVersion,
        checklistItemId = checklistItemId,
        checklistItemVersion = checklistItemVersion,
        goUserId = userId,
        userAnswer = try { Answer.valueOf(answer).ordinal } catch (e: Exception) { 0 },
        isDirty = isDirty,
        isNew = isNew,
        isMarkedForDeletion = false,
        userComment = userComment,
        businessId = businessId
    ) 