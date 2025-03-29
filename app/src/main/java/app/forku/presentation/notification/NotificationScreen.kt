package app.forku.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.notification.Notification
import app.forku.domain.model.notification.NotificationPriority
import app.forku.domain.model.notification.NotificationType
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanString

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { viewModel.loadNotifications(true) }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Notifications",
        showBottomBar = false,
        onRefresh = { viewModel.loadNotifications(true) },
        showLoadingOnRefresh = false,
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = { viewModel.loadNotifications(true) }
                )
                state.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications yet",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.notifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { viewModel.onNotificationClick(notification) }
                            )
                        }
                    }
                }
            }

            if (state.isLoading && state.isRefreshing) {
                PullRefreshIndicator(
                    refreshing = true,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) Color(0xFFF5F5F5) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when (notification.priority) {
                            NotificationPriority.URGENT -> Color.Red
                            NotificationPriority.HIGH -> Color(0xFFF44336)
                            NotificationPriority.MEDIUM -> Color(0xFFFFA726)
                            NotificationPriority.LOW -> Color(0xFF4CAF50)
                        },
                        CircleShape
                    )
            )
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (notification.type) {
                            NotificationType.INCIDENT -> "Incident"
                            NotificationType.SAFETY_ALERT -> "Safety Alert"
                            NotificationType.REMINDER -> "Reminder"
                            NotificationType.SYSTEM -> "System"
                            NotificationType.TRAINING -> "Training"
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = getRelativeTimeSpanString(notification.createdAt.toString()),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
        }
    }
} 