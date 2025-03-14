package app.forku.core.notification

import app.forku.core.notification.NotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTestService @Inject constructor(
    private val notificationManager: NotificationManager
) {
    // Development/Testing methods
    fun simulateIncidentNotification(
        incidentId: String = "test_incident_${System.currentTimeMillis()}",
        title: String = "Test Incident",
        message: String = "This is a test incident notification"
    ) {
        notificationManager.showIncidentNotification(
            incidentId = incidentId,
            title = title,
            message = message
        )
    }

    fun simulateSafetyAlert(
        alertId: String = "test_alert_${System.currentTimeMillis()}",
        title: String = "Test Alert",
        message: String = "This is a test safety alert"
    ) {
        notificationManager.showSafetyAlert(
            alertId = alertId,
            title = title,
            message = message
        )
    }

    fun simulateGeneralNotification(
        title: String = "Test Notification",
        message: String = "This is a test general notification"
    ) {
        notificationManager.showNotification(
            title = title,
            message = message
        )
    }
} 