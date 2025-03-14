package app.forku.core.notification

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for push notification services (FCM, OneSignal, etc.)
 * Implementations should handle the specifics of each service while maintaining
 * a consistent API for the rest of the application.
 */
interface PushNotificationService {
    /**
     * Current device token for push notifications
     */
    val deviceToken: StateFlow<String?>
    
    /**
     * Updates the device token for push notifications
     * @throws Exception if token update fails
     */
    suspend fun updateDeviceToken()
    
    /**
     * Sends a push notification
     * @param type The type of notification to send
     * @param data Additional data to be sent with the notification
     * @throws Exception if sending fails
     */
    suspend fun sendPushNotification(
        type: NotificationType,
        data: Map<String, String>
    )
    
    /**
     * Subscribes to a notification topic
     * @param topic The topic to subscribe to
     * @throws Exception if subscription fails
     */
    suspend fun subscribeToTopic(topic: String)
    
    /**
     * Unsubscribes from a notification topic
     * @param topic The topic to unsubscribe from
     * @throws Exception if unsubscription fails
     */
    suspend fun unsubscribeFromTopic(topic: String)
} 