package app.forku.core.notification

import app.forku.core.notification.PushNotificationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class ForkUMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "ForkUMessagingService"
    }
    
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var pushNotificationService: PushNotificationService

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        try {
            Log.d(TAG, "Message received from: ${message.from}")
            
            val data = message.data
            Log.d(TAG, "Message data: $data")
            
            val type = data["type"] ?: run {
                Log.w(TAG, "Message type not found")
                return
            }
            
            val id = data["id"] ?: ""
            val title = message.notification?.title ?: data["title"] ?: run {
                Log.w(TAG, "Message title not found")
                return
            }
            val messageText = message.notification?.body ?: data["message"] ?: run {
                Log.w(TAG, "Message body not found")
                return
            }

            when (type) {
                "INCIDENT" -> {
                    Log.d(TAG, "Showing incident notification: $id")
                    notificationManager.showIncidentNotification(id, title, messageText)
                }
                "SAFETY_ALERT" -> {
                    Log.d(TAG, "Showing safety alert: $id")
                    notificationManager.showSafetyAlert(id, title, messageText)
                }
                else -> {
                    Log.d(TAG, "Showing general notification")
                    notificationManager.showNotification(title, messageText)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received")
        
        serviceScope.launch {
            try {
                pushNotificationService.updateDeviceToken()
                Log.d(TAG, "Device token updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update device token", e)
            }
        }
    }
} 