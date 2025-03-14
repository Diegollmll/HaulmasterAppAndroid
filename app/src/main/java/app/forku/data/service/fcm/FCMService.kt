package app.forku.data.service.fcm

import app.forku.core.notification.NotificationManager
import app.forku.core.notification.NotificationType
import app.forku.core.notification.PushNotificationService
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

@Singleton
class FCMService @Inject constructor(
    private val notificationManager: NotificationManager,
    private val firebaseMessaging: FirebaseMessaging
) : PushNotificationService {
    private val firebaseFunctions = Firebase.functions
    
    private val _deviceToken = MutableStateFlow<String?>(null)
    override val deviceToken: StateFlow<String?> = _deviceToken

    override suspend fun updateDeviceToken() = withContext(Dispatchers.IO) {
        try {
            val token = notificationManager.getFCMToken()
            _deviceToken.value = token
        } catch (e: Exception) {
            throw Exception("Failed to update FCM token: ${e.message}", e)
        }
    }

    override suspend fun sendPushNotification(
        type: NotificationType,
        data: Map<String, String>
    ) = withContext(Dispatchers.IO) {
        val maxRetries = 3
        var currentRetry = 0
        var lastException: Exception? = null
        
        while (currentRetry < maxRetries) {
            try {
                val payload = hashMapOf(
                    "type" to type.type,
                    "data" to data
                )
                
                withTimeout(30000) { // 30 seconds timeout
                    firebaseFunctions
                        .getHttpsCallable("sendPushNotification")
                        .call(payload)
                        .await()
                }
                return@withContext // Success, exit the function
            } catch (e: InterruptedException) {
                throw Exception("Push notification operation was interrupted", e)
            } catch (e: Exception) {
                lastException = e
                currentRetry++
                if (currentRetry < maxRetries) {
                    delay(currentRetry * 1000L) // Exponential backoff
                    continue
                }
            }
        }
        
        throw Exception("Failed to send push notification after $maxRetries retries: ${lastException?.message}", lastException)
    }
    
    override suspend fun subscribeToTopic(topic: String) {
        withContext(Dispatchers.IO) {
            try {
                firebaseMessaging.subscribeToTopic(topic).await()
            } catch (e: Exception) {
                throw Exception("Failed to subscribe to topic '$topic': ${e.message}", e)
            }
        }
    }
    
    override suspend fun unsubscribeFromTopic(topic: String) {
        withContext(Dispatchers.IO) {
            try {
                firebaseMessaging.unsubscribeFromTopic(topic).await()
            } catch (e: Exception) {
                throw Exception("Failed to unsubscribe from topic '$topic': ${e.message}", e)
            }
        }
    }
} 