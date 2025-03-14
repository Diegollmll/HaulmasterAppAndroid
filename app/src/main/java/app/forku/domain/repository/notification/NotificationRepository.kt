package app.forku.domain.repository.notification

import app.forku.domain.model.notification.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun getNotifications(): List<Notification>
    suspend fun getNotificationById(id: String): Notification?
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun createNotification(notification: Notification): Notification
    suspend fun updateNotification(id: String, notification: Notification): Notification
    suspend fun getUnreadCount(): Int
    fun observeNotifications(): Flow<List<Notification>>
} 