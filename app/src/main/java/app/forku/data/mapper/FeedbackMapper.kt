package app.forku.data.mapper

import app.forku.data.dto.FeedbackDto
import app.forku.domain.model.feedback.Feedback

fun FeedbackDto.toFeedback(): Feedback = Feedback(
    id = id,
    userId = userId,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Feedback.toFeedbackDto(): FeedbackDto = FeedbackDto(
    id = id,
    userId = userId,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt
) 