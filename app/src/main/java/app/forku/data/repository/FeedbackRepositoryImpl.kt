package app.forku.data.repository

import app.forku.data.api.FeedbackApi
import app.forku.data.mapper.toFeedback
import app.forku.data.mapper.toFeedbackDto
import app.forku.domain.model.feedback.Feedback
import app.forku.domain.repository.feedback.FeedbackRepository
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val api: FeedbackApi
) : FeedbackRepository {
    override suspend fun submitFeedback(feedback: Feedback): Result<Feedback> = runCatching {
        api.submitFeedback(feedback.toFeedbackDto()).toFeedback()
    }

    override suspend fun getFeedbacks(): Result<List<Feedback>> = runCatching {
        api.getFeedbacks().map { it.toFeedback() }
    }

    override suspend fun getFeedback(id: String): Result<Feedback> = runCatching {
        api.getFeedback(id).toFeedback()
    }

    override suspend fun updateFeedback(feedback: Feedback): Result<Feedback> = runCatching {
        api.updateFeedback(feedback.id!!, feedback.toFeedbackDto()).toFeedback()
    }

    override suspend fun deleteFeedback(id: String): Result<Unit> = runCatching {
        api.deleteFeedback(id)
    }
} 