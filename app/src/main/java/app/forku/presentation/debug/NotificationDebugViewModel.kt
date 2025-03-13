package app.forku.presentation.debug

import androidx.lifecycle.ViewModel
import app.forku.data.repository.notification.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationDebugViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val fcmToken = notificationRepository.fcmToken

    fun testIncidentNotification() {
        notificationRepository.simulateIncidentNotification()
    }

    fun testSafetyAlert() {
        notificationRepository.simulateSafetyAlert()
    }

    fun testGeneralNotification() {
        notificationRepository.simulateGeneralNotification()
    }

    // Custom notification test
    fun testCustomNotification(
        type: String,
        id: String,
        title: String,
        message: String
    ) {
        when (type) {
            "INCIDENT" -> notificationRepository.simulateIncidentNotification(id, title, message)
            "SAFETY_ALERT" -> notificationRepository.simulateSafetyAlert(id, title, message)
            else -> notificationRepository.simulateGeneralNotification(title, message)
        }
    }
} 