package app.forku.data.api

import app.forku.data.api.dto.FeedbackDto
import retrofit2.Response
import retrofit2.http.*

interface FeedbackApi {
    @POST("api/feedback")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun submitFeedback(@Body feedback: FeedbackDto): Response<FeedbackDto>

    @GET("api/feedback/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbacks(): Response<List<FeedbackDto>>

    @GET("api/feedback/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedback(@Path("id") id: String): Response<FeedbackDto>

    @PUT("api/feedback/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun updateFeedback(
        @Path("id") id: String,
        @Body feedback: FeedbackDto
    ): Response<FeedbackDto>

    @DELETE("dataset/api/feedback/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteFeedback(@Path("id") id: String): Response<Unit>

    // Feedback analytics endpoint (example: get feedback stats)
    @GET("api/feedback/analytics")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbackAnalytics(): Response<Map<String, Any>>
} 