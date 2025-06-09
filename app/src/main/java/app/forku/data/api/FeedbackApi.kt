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
    suspend fun getFeedbacks(): Response<List<FeedbackDto>>

    /**
     * Get a list of Feedback instances in dataset format
     */
    @GET("dataset/api/feedback/list")
    suspend fun getFeedbacksDataset(): Response<List<FeedbackDto>>

    /**
     * Get a Feedback instance by ID in json structured format
     */
    @GET("api/feedback/byid/{id}")
    suspend fun getFeedbackById(@Path("id") id: String): Response<FeedbackDto>

    /**
     * Get a Feedback instance by ID in dataset format
     */
    @GET("dataset/api/feedback/byid/{id}")
    suspend fun getFeedbackByIdDataset(@Path("id") id: String): Response<FeedbackDto>

    /**
     * Save (create or update) a Feedback instance in json format
     */
    @FormUrlEncoded
    @POST("api/feedback")
    suspend fun saveFeedback(
        @Field("entity") entity: String
    ): Response<FeedbackDto>

    /**
     * Save (create or update) a Feedback instance in dataset format
     */
    @FormUrlEncoded
    @POST("dataset/api/feedback")
    suspend fun saveFeedbackDataset(
        @Field("entity") entity: String
    ): Response<FeedbackDto>

    /**
     * Delete a Feedback instance by ID
     */
    @DELETE("dataset/api/feedback/{id}")
    suspend fun deleteFeedback(@Path("id") id: String): Response<Unit>

    /**
     * Delete a Feedback instance
     */
    @DELETE("dataset/api/feedback")
    suspend fun deleteFeedback(@Body feedback: FeedbackDto): Response<Unit>

    /**
     * Count the number of Feedback instances
     */
    @GET("dataset/api/feedback/count")
    suspend fun getFeedbackCount(): Response<Int>
} 