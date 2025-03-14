package app.forku.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.notification.Notification
import app.forku.domain.repository.notification.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationState())
    val state = _state.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = showLoading,
                    isRefreshing = showLoading
                ) }

                val notifications = notificationRepository.getNotifications()
                _state.update { 
                    it.copy(
                        notifications = notifications,
                        unreadCount = notifications.count { notification -> !notification.isRead },
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to load notifications",
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun onNotificationClick(notification: Notification) {
        viewModelScope.launch {
            try {
                // Mark as read if not already read
                if (!notification.isRead) {
                    notificationRepository.markAsRead(notification.id)
                }

                // Refresh notifications to update read status
                loadNotifications(false)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun getUnreadCount(): Int {
        return state.value.unreadCount
    }
} 