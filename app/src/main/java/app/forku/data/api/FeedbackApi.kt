package app.forku.data.api

import app.forku.data.api.dto.FeedbackDto
import retrofit2.http.*

interface FeedbackApi {
    @POST("feedback")
    suspend fun submitFeedback(@Body feedback: FeedbackDto): FeedbackDto

    @GET("feedback")
    suspend fun getFeedbacks(): List<FeedbackDto>

    @GET("feedback/{id}")
    suspend fun getFeedback(@Path("id") id: String): FeedbackDto

    @PUT("feedback/{id}")
    suspend fun updateFeedback(
        @Path("id") id: String,
        @Body feedback: FeedbackDto
    ): FeedbackDto

    @DELETE("feedback/{id}")
    suspend fun deleteFeedback(@Path("id") id: String)
} 