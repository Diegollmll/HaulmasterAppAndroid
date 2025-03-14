package app.forku.domain.repository.notification

interface LocalNotificationManager {
    fun showIncidentNotification(incidentId: String, title: String, message: String)
    fun showSafetyAlert(alertId: String, title: String, message: String)
    fun showNotification(title: String, message: String)
} 