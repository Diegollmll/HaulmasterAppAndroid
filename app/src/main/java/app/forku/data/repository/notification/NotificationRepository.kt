package app.forku.data.repository.notification

import app.forku.core.notification.NotificationManager
import app.forku.core.notification.NotificationType
import app.forku.core.notification.PushNotificationService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository for handling all notification operations.
 * This includes both push notifications (via PushNotificationService)
 * and local notifications (via NotificationManager).
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val notificationManager: NotificationManager,
    private val pushNotificationService: PushNotificationService
) {
    val deviceToken: StateFlow<String?> = pushNotificationService.deviceToken

    // Local Notifications
    fun showIncidentNotification(incidentId: String, title: String, message: String) {
        notificationManager.showIncidentNotification(
            incidentId = incidentId,
            title = title,
            message = message
        )
    }

    fun showSafetyAlert(alertId: String, title: String, message: String) {
        notificationManager.showSafetyAlert(
            alertId = alertId,
            title = title,
            message = message
        )
    }

    fun showNotification(title: String, message: String) {
        notificationManager.showNotification(
            title = title,
            message = message
        )
    }

    // Push Notifications
    suspend fun sendIncidentNotification(incidentId: String, title: String, message: String) {
        pushNotificationService.sendPushNotification(
            type = NotificationType.Incident,
            data = mapOf(
                "incidentId" to incidentId,
                "title" to title,
                "message" to message
            )
        )
    }

    suspend fun sendSafetyAlert(alertId: String, title: String, message: String) {
        pushNotificationService.sendPushNotification(
            type = NotificationType.Safety,
            data = mapOf(
                "alertId" to alertId,
                "title" to title,
                "message" to message
            )
        )
    }

    suspend fun sendNotification(title: String, message: String) {
        pushNotificationService.sendPushNotification(
            type = NotificationType.General,
            data = mapOf(
                "title" to title,
                "message" to message
            )
        )
    }

    // Token Management
    suspend fun updateDeviceToken() {
        pushNotificationService.updateDeviceToken()
    }

    // Topic Management
    suspend fun subscribeToTopic(topic: String) {
        pushNotificationService.subscribeToTopic(topic)
    }

    suspend fun unsubscribeFromTopic(topic: String) {
        pushNotificationService.unsubscribeFromTopic(topic)
    }
} 