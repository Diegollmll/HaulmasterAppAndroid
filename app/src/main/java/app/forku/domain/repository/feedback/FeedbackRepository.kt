package app.forku.domain.repository.feedback

import app.forku.domain.model.feedback.Feedback
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: Feedback): Result<Feedback>
    suspend fun getFeedbacks(): Result<List<Feedback>>
    suspend fun getFeedback(id: String): Result<Feedback>
    suspend fun updateFeedback(feedback: Feedback): Result<Feedback>
    suspend fun deleteFeedback(id: String): Result<Unit>
    suspend fun getFeedbackCount(): Result<Int>
} 