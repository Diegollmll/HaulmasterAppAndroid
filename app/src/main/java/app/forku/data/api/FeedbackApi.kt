package app.forku.data.api

import app.forku.data.api.dto.FeedbackDto
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for Feedback endpoints.
 * All endpoints follow the pattern /api/feedback/* */ for standard operations
 * and /dataset/api/feedback/* */ for data operations.
 */
interface FeedbackApi {
    /**
     * Get a list of Feedback instances in json structured format
     */
    @GET("api/feedback/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbacks(): Response<List<FeedbackDto>>

    /**
     * Get a list of Feedback instances in dataset format
     */
    @GET("dataset/api/feedback/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbacksDataset(): Response<List<FeedbackDto>>

    /**
     * Get a Feedback instance by ID in json structured format
     */
    @GET("api/feedback/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbackById(@Path("id") id: String): Response<FeedbackDto>

    /**
     * Get a Feedback instance by ID in dataset format
     */
    @GET("dataset/api/feedback/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbackByIdDataset(@Path("id") id: String): Response<FeedbackDto>

    /**
     * Save (create or update) a Feedback instance in json format
     */
    @POST("api/feedback")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveFeedback(@Body feedback: FeedbackDto): Response<FeedbackDto>

    /**
     * Save (create or update) a Feedback instance in dataset format
     */
    @POST("dataset/api/feedback")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveFeedbackDataset(@Body feedback: FeedbackDto): Response<FeedbackDto>

    /**
     * Delete a Feedback instance by ID
     */
    @DELETE("dataset/api/feedback/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteFeedback(@Path("id") id: String): Response<Unit>

    /**
     * Count the number of Feedback instances
     */
    @GET("dataset/api/feedback/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbackCount(): Response<Int>

    /**
     * Get feedback analytics
     */
    @GET("api/feedback/analytics")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getFeedbackAnalytics(): Response<Map<String, Any>>
} 