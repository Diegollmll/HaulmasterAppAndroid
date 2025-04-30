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
        val response = api.saveFeedback(feedback.toFeedbackDto())
        if (response.isSuccessful) {
            response.body()?.toFeedback() ?: throw Exception("Response body is null")
        } else {
            throw Exception("Failed to submit feedback: ${response.code()}")
        }
    }

    override suspend fun getFeedbacks(): Result<List<Feedback>> = runCatching {
        val response = api.getFeedbacks()
        if (response.isSuccessful) {
            response.body()?.map { it.toFeedback() } ?: emptyList()
        } else {
            throw Exception("Failed to get feedbacks: ${response.code()}")
        }
    }

    override suspend fun getFeedback(id: String): Result<Feedback> = runCatching {
        val response = api.getFeedbackById(id)
        if (response.isSuccessful) {
            response.body()?.toFeedback() ?: throw Exception("Response body is null")
        } else {
            throw Exception("Failed to get feedback: ${response.code()}")
        }
    }

    override suspend fun updateFeedback(feedback: Feedback): Result<Feedback> = runCatching {
        val response = api.saveFeedback(feedback.toFeedbackDto())
        if (response.isSuccessful) {
            response.body()?.toFeedback() ?: throw Exception("Response body is null")
        } else {
            throw Exception("Failed to update feedback: ${response.code()}")
        }
    }

    override suspend fun deleteFeedback(id: String): Result<Unit> = runCatching {
        val response = api.deleteFeedback(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to delete feedback: ${response.code()}")
        }
    }

    override suspend fun getFeedbackCount(): Result<Int> = runCatching {
        val response = api.getFeedbackCount()
        if (response.isSuccessful) {
            response.body() ?: 0
        } else {
            throw Exception("Failed to get feedback count: ${response.code()}")
        }
    }

    override suspend fun getFeedbackAnalytics(): Result<Map<String, Any>> = runCatching {
        val response = api.getFeedbackAnalytics()
        if (response.isSuccessful) {
            response.body() ?: emptyMap()
        } else {
            throw Exception("Failed to get feedback analytics: ${response.code()}")
        }
    }
} 