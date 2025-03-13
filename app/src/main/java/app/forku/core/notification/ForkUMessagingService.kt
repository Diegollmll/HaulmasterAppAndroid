package app.forku.core.notification

import app.forku.data.repository.notification.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForkUMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        // Handle data payload
        message.data.let { data ->
            when (data["type"]) {
                "INCIDENT" -> {
                    notificationManager.showIncidentNotification(
                        incidentId = data["incidentId"] ?: return,
                        title = data["title"] ?: "New Incident",
                        message = data["message"] ?: "A new incident has been reported"
                    )
                }
                "SAFETY_ALERT" -> {
                    notificationManager.showSafetyAlert(
                        alertId = data["alertId"] ?: return,
                        title = data["title"] ?: "Safety Alert",
                        message = data["message"] ?: "New safety alert"
                    )
                }
                else -> {
                    // Handle notification payload
                    message.notification?.let { notification ->
                        notificationManager.showNotification(
                            title = notification.title ?: "ForkU Notification",
                            message = notification.body ?: "You have a new notification"
                        )
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update token in repository
        serviceScope.launch {
            notificationRepository.updateFCMToken()
        }
    }
} 