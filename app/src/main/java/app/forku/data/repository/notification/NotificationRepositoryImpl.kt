package app.forku.data.repository.notification

import app.forku.data.api.NotificationApi
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
    private val api: NotificationApi
) : NotificationRepository {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

    override suspend fun getNotifications(): List<Notification> {
        return try {
            val response = api.getNotifications()
            if (response.isSuccessful) {
                val notifications = response.body()?.map { it.toNotification() } ?: emptyList()
                _notifications.emit(notifications)
                notifications
            } else {
                throw Exception("Failed to fetch notifications: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch notifications: ${e.message}")
        }
    }

    override suspend fun getNotificationById(id: String): Notification? {
        return try {
            val response = api.getNotificationById(id)
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
            val response = api.saveNotification(updatedNotification.toNotificationDto())
            if (!response.isSuccessful) {
                throw Exception("Failed to mark notification as read: ${response.code()}")
            }
            // Update local state
            refreshNotifications()
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
            // Update local state
            refreshNotifications()
        } catch (e: Exception) {
            throw Exception("Failed to mark all notifications as read: ${e.message}")
        }
    }

    override suspend fun deleteNotification(id: String) {
        try {
            val response = api.deleteNotification(id)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete notification: ${response.code()}")
            }
            // Update local state
            refreshNotifications()
        } catch (e: Exception) {
            throw Exception("Failed to delete notification: ${e.message}")
        }
    }

    override suspend fun createNotification(notification: Notification): Notification {
        try {
            val response = api.saveNotification(notification.toNotificationDto())
            if (!response.isSuccessful) {
                throw Exception("Failed to create notification: ${response.code()}")
            }
            
            val createdNotification = response.body()?.toNotification()
                ?: throw Exception("Created notification response was null")
            
            // Update local state
            refreshNotifications()
            return createdNotification
        } catch (e: Exception) {
            throw Exception("Failed to create notification: ${e.message}")
        }
    }

    override suspend fun updateNotification(id: String, notification: Notification): Notification {
        try {
            val response = api.saveNotification(notification.toNotificationDto())
            if (!response.isSuccessful) {
                throw Exception("Failed to update notification: ${response.code()}")
            }
            
            val updatedNotification = response.body()?.toNotification()
                ?: throw Exception("Updated notification response was null")
            
            // Update local state
            refreshNotifications()
            return updatedNotification
        } catch (e: Exception) {
            throw Exception("Failed to update notification: ${e.message}")
        }
    }

    override suspend fun getUnreadCount(): Int {
        return try {
            val notifications = getNotifications()
            notifications.count { !it.isRead }
        } catch (e: Exception) {
            0
        }
    }

    override fun observeNotifications(): Flow<List<Notification>> {
        return _notifications.asStateFlow()
    }

    private suspend fun refreshNotifications() {
        try {
            val notifications = getNotifications()
            _notifications.emit(notifications)
        } catch (e: Exception) {
            // Log error but don't throw to avoid crashing the app
            e.printStackTrace()
        }
    }
} 