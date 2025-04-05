package app.forku.data.api

import app.forku.data.api.dto.notification.NotificationDto
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @GET("notifications/{id}")
    suspend fun getNotification(
        @Path("id") id: String
    ): Response<NotificationDto>

    @POST("notifications")
    suspend fun createNotification(
        @Body notification: NotificationDto
    ): Response<NotificationDto>

    @PUT("notifications/{id}")
    suspend fun updateNotification(
        @Path("id") id: String,
        @Body notification: NotificationDto
    ): Response<NotificationDto>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") id: String
    ): Response<Unit>
} 