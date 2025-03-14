package app.forku.data.mapper

import app.forku.data.api.dto.notification.NotificationDto
import app.forku.domain.model.notification.Notification
import app.forku.domain.model.notification.NotificationType
import app.forku.domain.model.notification.NotificationPriority
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun NotificationDto.toNotification(): Notification {
    return Notification(
        id = id,
        userId = userId,
        type = NotificationType.valueOf(type.uppercase()),
        title = title,
        message = message,
        priority = NotificationPriority.valueOf(priority.uppercase()),
        isRead = isRead,
        createdAt = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME),
        data = data
    )
}

fun Notification.toNotificationDto(): NotificationDto {
    return NotificationDto(
        id = id,
        userId = userId,
        type = type.name,
        title = title,
        message = message,
        priority = priority.name,
        isRead = isRead,
        createdAt = createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
        data = data
    )
} 