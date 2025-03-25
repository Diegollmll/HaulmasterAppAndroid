package app.forku.data.api

import app.forku.data.dto.FeedbackDto
import retrofit2.http.*

interface FeedbackApi {
    @POST("feedbacks")
    suspend fun submitFeedback(@Body feedback: FeedbackDto): FeedbackDto

    @GET("feedbacks")
    suspend fun getFeedbacks(): List<FeedbackDto>

    @GET("feedbacks/{id}")
    suspend fun getFeedback(@Path("id") id: String): FeedbackDto

    @PUT("feedbacks/{id}")
    suspend fun updateFeedback(
        @Path("id") id: String,
        @Body feedback: FeedbackDto
    ): FeedbackDto

    @DELETE("feedbacks/{id}")
    suspend fun deleteFeedback(@Path("id") id: String)
} 