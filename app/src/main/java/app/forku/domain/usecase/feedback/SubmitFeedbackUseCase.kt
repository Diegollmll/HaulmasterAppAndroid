package app.forku.domain.usecase.feedback

import app.forku.domain.model.feedback.Feedback
import app.forku.domain.repository.feedback.FeedbackRepository
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject

class SubmitFeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(rating: Int, comment: String, canContactMe: Boolean): Result<Feedback> {
        return try {
            val currentUser = userRepository.getCurrentUser()
                ?: return Result.failure(Exception("User not authenticated"))

            val feedback = Feedback(
                goUserId = currentUser.id,
                rating = rating,
                comment = comment,
                canContactMe = canContactMe
            )

            feedbackRepository.submitFeedback(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 