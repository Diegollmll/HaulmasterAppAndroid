package app.forku.domain.model.notification

import java.time.LocalDateTime

enum class NotificationType {
    INCIDENT,
    SAFETY_ALERT,
    REMINDER,
    SYSTEM,
    TRAINING
}

enum class NotificationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val priority: NotificationPriority,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val data: Map<String, String>? = null
) 