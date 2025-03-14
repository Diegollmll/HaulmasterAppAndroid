package app.forku.domain.repository.notification

import kotlinx.coroutines.flow.Flow

interface PushNotificationManager {
    suspend fun sendIncidentNotification(incidentId: String, title: String, message: String)
    suspend fun sendSafetyAlert(alertId: String, title: String, message: String)
    suspend fun sendNotification(title: String, message: String)
    
    // Token Management
    val deviceToken: Flow<String?>
    suspend fun updateDeviceToken()
    
    // Topic Management
    suspend fun subscribeToTopic(topic: String)
    suspend fun unsubscribeFromTopic(topic: String)
} 