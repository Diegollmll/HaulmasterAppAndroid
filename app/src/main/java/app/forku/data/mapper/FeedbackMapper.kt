package app.forku.data.mapper

import app.forku.data.api.dto.FeedbackDto
import app.forku.domain.model.feedback.Feedback

fun FeedbackDto.toFeedback(): Feedback = Feedback(
    id = id,
    canContactMe = canContactMe,
    comment = comment,
    goUserId = goUserId,
    rating = rating
)

fun Feedback.toFeedbackDto(): FeedbackDto = FeedbackDto(
    id = id,
    canContactMe = canContactMe,
    comment = comment,
    goUserId = goUserId,
    rating = rating,
    isDirty = true,
    isNew = true,
    isMarkedForDeletion = false
) 