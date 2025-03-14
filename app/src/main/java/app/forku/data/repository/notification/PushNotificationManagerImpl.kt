package app.forku.data.repository.notification

import app.forku.core.notification.NotificationType
import app.forku.core.notification.PushNotificationService
import app.forku.domain.repository.notification.PushNotificationManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationManagerImpl @Inject constructor(
    private val pushNotificationService: PushNotificationService
) : PushNotificationManager {
    
    override suspend fun sendIncidentNotification(incidentId: String, title: String, message: String) {
        pushNotificationService.sendPushNotification(
            type = NotificationType.Incident,
            data = mapOf(
                "incidentId" to incidentId,
                "title" to title,
                "message" to message
            )
        )
    }

    override suspend fun sendSafetyAlert(alertId: String, title: String, message: String) {
        pushNotificationService.sendPushNotification(
            type = NotificationType.Safety,
            data = mapOf(
                "alertId" to alertId,
                "title" to title,
                "message" to message
            )
        )
    }

    override suspend fun sendNotification(title: String, message: String) {
        pushNotificationService.sendPushNotification(
            type = NotificationType.General,
            data = mapOf(
                "title" to title,
                "message" to message
            )
        )
    }

    override val deviceToken: Flow<String?> = pushNotificationService.deviceToken

    override suspend fun updateDeviceToken() {
        pushNotificationService.updateDeviceToken()
    }

    override suspend fun subscribeToTopic(topic: String) {
        pushNotificationService.subscribeToTopic(topic)
    }

    override suspend fun unsubscribeFromTopic(topic: String) {
        pushNotificationService.unsubscribeFromTopic(topic)
    }
} 