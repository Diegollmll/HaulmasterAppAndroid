package app.forku.data.api

import app.forku.data.api.dto.notification.NotificationDto
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for Notification endpoints.
 * All endpoints follow the pattern /api/notification/* */ for standard operations
 * and /dataset/api/notification/* */ for data operations.
 */
interface NotificationApi {
    /**
     * Get a list of Notification instances in json structured format
     */
    @GET("api/notification/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotifications(): Response<List<NotificationDto>>

    /**
     * Get a list of Notification instances in dataset format
     */
    @GET("dataset/api/notification/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotificationsDataset(): Response<List<NotificationDto>>

    /**
     * Get a Notification instance by ID in json structured format
     */
    @GET("api/notification/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotificationById(@Path("id") id: String): Response<NotificationDto>

    /**
     * Get a Notification instance by ID in dataset format
     */
    @GET("dataset/api/notification/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotificationByIdDataset(@Path("id") id: String): Response<NotificationDto>

    /**
     * Save (create or update) a Notification instance in json format
     */
    @POST("api/notification")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveNotification(@Body notification: NotificationDto): Response<NotificationDto>

    /**
     * Save (create or update) a Notification instance in dataset format
     */
    @POST("dataset/api/notification")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveNotificationDataset(@Body notification: NotificationDto): Response<NotificationDto>

    /**
     * Delete a Notification instance by ID
     */
    @DELETE("dataset/api/notification/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteNotification(@Path("id") id: String): Response<Unit>

    /**
     * Count the number of Notification instances
     */
    @GET("dataset/api/notification/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotificationCount(): Response<Int>
} 