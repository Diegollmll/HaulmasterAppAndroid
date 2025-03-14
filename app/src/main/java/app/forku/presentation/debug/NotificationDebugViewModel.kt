package app.forku.presentation.debug

import androidx.lifecycle.ViewModel
import app.forku.core.notification.NotificationTestService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationDebugViewModel @Inject constructor(
    private val notificationTestService: NotificationTestService
) : ViewModel() {

    fun simulateIncidentNotification() {
        notificationTestService.simulateIncidentNotification()
    }

    fun simulateSafetyAlert() {
        notificationTestService.simulateSafetyAlert()
    }

    fun simulateGeneralNotification() {
        notificationTestService.simulateGeneralNotification()
    }

    // Custom notification test
    fun testCustomNotification(
        type: String,
        id: String,
        title: String,
        message: String
    ) {
        when (type) {
            "INCIDENT" -> notificationTestService.simulateIncidentNotification(id, title, message)
            "SAFETY_ALERT" -> notificationTestService.simulateSafetyAlert(id, title, message)
            else -> notificationTestService.simulateGeneralNotification(title, message)
        }
    }
} 