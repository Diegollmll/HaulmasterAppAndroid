package app.forku.core.notification

import app.forku.data.repository.notification.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend fun sendIncidentNotification(
        incidentId: String,
        title: String,
        message: String
    ) {
        notificationRepository.showIncidentNotification(
            incidentId = incidentId,
            title = title,
            message = message
        )
        
        notificationRepository.sendIncidentNotification(
            incidentId = incidentId,
            title = title,
            message = message
        )
    }

    suspend fun sendSafetyAlert(
        alertId: String,
        title: String,
        message: String
    ) {
        notificationRepository.showSafetyAlert(
            alertId = alertId,
            title = title,
            message = message
        )
        
        notificationRepository.sendSafetyAlert(
            alertId = alertId,
            title = title,
            message = message
        )
    }

    suspend fun sendGeneralNotification(
        title: String,
        message: String
    ) {
        notificationRepository.showNotification(
            title = title,
            message = message
        )
        
        notificationRepository.sendNotification(
            title = title,
            message = message
        )
    }
} 