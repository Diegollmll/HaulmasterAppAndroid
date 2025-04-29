package app.forku.data.api

import app.forku.data.api.dto.notification.NotificationDto
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    @GET("api/notification/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @GET("api/notification/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotification(
        @Path("id") id: String
    ): Response<NotificationDto>

    @POST("api/notification")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun createNotification(
        @Body notification: NotificationDto
    ): Response<NotificationDto>

    @PUT("api/notification/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun updateNotification(
        @Path("id") id: String,
        @Body notification: NotificationDto
    ): Response<NotificationDto>

    @DELETE("dataset/api/notification/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteNotification(
        @Path("id") id: String
    ): Response<Unit>

    @GET("api/notification/preferences")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getNotificationPreferences(): Response<Map<String, Any>>

    @POST("api/notification/preferences")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveNotificationPreferences(
        @Body preferences: Map<String, Any>
    ): Response<Unit>
} 