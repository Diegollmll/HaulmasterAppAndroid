package app.forku.data.repository.notification

import app.forku.core.notification.NotificationManager
import app.forku.domain.repository.notification.LocalNotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationManagerImpl @Inject constructor(
    private val notificationManager: NotificationManager
) : LocalNotificationManager {
    
    override fun showIncidentNotification(incidentId: String, title: String, message: String) {
        notificationManager.showIncidentNotification(incidentId, title, message)
    }

    override fun showSafetyAlert(alertId: String, title: String, message: String) {
        notificationManager.showSafetyAlert(alertId, title, message)
    }

    override fun showNotification(title: String, message: String) {
        notificationManager.showNotification(title, message)
    }
} 