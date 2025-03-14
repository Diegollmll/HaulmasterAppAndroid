package app.forku.data.repository.notification

import app.forku.data.api.GeneralApi
import app.forku.data.mapper.toNotification
import app.forku.data.mapper.toNotificationDto
import app.forku.domain.model.notification.Notification
import app.forku.domain.repository.notification.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: GeneralApi
) : NotificationRepository {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

    override suspend fun getNotifications(): List<Notification> {
        return try {
            val response = api.getNotifications()
            if (response.isSuccessful) {
                response.body()?.map { it.toNotification() } ?: emptyList()
            } else {
                throw Exception("Failed to fetch notifications")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch notifications: ${e.message}")
        }
    }

    override suspend fun getNotificationById(id: String): Notification? {
        return try {
            val response = api.getNotification(id)
            if (response.isSuccessful) {
                response.body()?.toNotification()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun markAsRead(id: String) {
        try {
            val notification = getNotificationById(id) ?: return
            val updatedNotification = notification.copy(isRead = true)
            api.updateNotification(id, updatedNotification.toNotificationDto())
        } catch (e: Exception) {
            throw Exception("Failed to mark notification as read: ${e.message}")
        }
    }

    override suspend fun markAllAsRead() {
        try {
            val notifications = getNotifications()
            notifications.forEach { notification ->
                if (!notification.isRead) {
                    markAsRead(notification.id)
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to mark all notifications as read: ${e.message}")
        }
    }

    override suspend fun deleteNotification(id: String) {
        try {
            api.deleteNotification(id)
        } catch (e: Exception) {
            throw Exception("Failed to delete notification: ${e.message}")
        }
    }

    override suspend fun createNotification(notification: Notification): Notification {
        try {
            val response = api.createNotification(notification.toNotificationDto())
            if (!response.isSuccessful) throw Exception("Failed to create notification")
            
            return response.body()?.toNotification()
                ?: throw Exception("Created notification response was null")
        } catch (e: Exception) {
            throw Exception("Failed to create notification: ${e.message}")
        }
    }

    override suspend fun updateNotification(id: String, notification: Notification): Notification {
        try {
            val response = api.updateNotification(id, notification.toNotificationDto())
            if (!response.isSuccessful) throw Exception("Failed to update notification")
            
            return response.body()?.toNotification()
                ?: throw Exception("Updated notification response was null")
        } catch (e: Exception) {
            throw Exception("Failed to update notification: ${e.message}")
        }
    }

    override suspend fun getUnreadCount(): Int {
        return getNotifications().count { !it.isRead }
    }

    override fun observeNotifications(): Flow<List<Notification>> {
        return _notifications.asStateFlow()
    }
} 