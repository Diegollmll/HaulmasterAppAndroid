package app.forku.presentation.notification

import app.forku.domain.model.notification.Notification

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val unreadCount: Int = 0
) 